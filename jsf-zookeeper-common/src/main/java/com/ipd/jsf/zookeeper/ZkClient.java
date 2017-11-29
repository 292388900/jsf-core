/**
 * Copyright 2004-2048 .
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ipd.jsf.zookeeper;

import com.ipd.jsf.zookeeper.exception.*;

import org.apache.zookeeper.*;
import org.apache.zookeeper.KeeperException.ConnectionLossException;
import org.apache.zookeeper.KeeperException.SessionExpiredException;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ZkClient implements Watcher {

	private Logger logger = LoggerFactory.getLogger(ZkClient.class);

	private ZooKeeper zookeeper;

	private volatile KeeperState _currentState;
	private final ZkLock _zkEventLock = new ZkLock();
	private Lock _zookeeperLock = new ReentrantLock();
	private volatile boolean _shutdownTriggered;
	private ZkEventThread _eventThread;
	private boolean started = false;

	private Thread _zookeeperEventThread;

	private final Map<String, Set<IZkChildListener>> _childListener = new ConcurrentHashMap<String, Set<IZkChildListener>>();
	private final ConcurrentHashMap<String, Set<IZkDataListener>> _dataListener = new ConcurrentHashMap<String, Set<IZkDataListener>>();
	private final Set<IZkStateListener> _stateListener = new CopyOnWriteArraySet<IZkStateListener>();

	/*
	 * 本身就是一个watcher
	 */

	private String _connStr;
	private long _connectionTimeout;
	private int _sessionTimeout;

	public ZkClient(String connStr, long connectionTimeout, int sessionTimeout)
			throws IOException {
		this._connStr = connStr;
		if(connectionTimeout < 12000) connectionTimeout = 12000;//默认connection 12秒
		if(sessionTimeout < 50000) sessionTimeout = 50000;
		this._connectionTimeout = connectionTimeout;
		this._sessionTimeout = sessionTimeout;
		
		connect();

	}

	private void connect() {

		try {
			getEventLock().lockInterruptibly();
			setShutdownTrigger(false);
			_eventThread = new ZkEventThread(_connStr);
			_eventThread.start();
			zkConnect(this);
			if (!waitUntilConnected(_connectionTimeout, TimeUnit.MILLISECONDS)) {
				throw new ZkTimeoutException(
						"Unable to connect to zookeeper "+ _connStr +" server within timeout: "
								+ _connectionTimeout);
			}
			started = true;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage());

		} finally {
			getEventLock().unlock();
			logger.info("ZkClient status:{} will continue try.",started);
			// we should close the zookeeper instance, otherwise it would keep
			// on trying to connect
			
			if (!started) {
				close();
			}
		}
	}
	
	public List<String> subscribeChildChanges(String path, IZkChildListener listener) {
        synchronized (_childListener) {
            Set<IZkChildListener> listeners = _childListener.get(path);
            if (listeners == null) {
                listeners = new CopyOnWriteArraySet<IZkChildListener>();
                _childListener.put(path, listeners);
            }
            listeners.add(listener);
        }
        return watchForChilds(path,true);
    }

    public void unsubscribeChildChanges(String path, IZkChildListener childListener) {
        synchronized (_childListener) {
            final Set<IZkChildListener> listeners = _childListener.get(path);
            if (listeners != null) {
                listeners.remove(childListener);
            }
        }
    }

    public void subscribeDataChanges(String path, IZkDataListener listener) {
        Set<IZkDataListener> listeners;
        synchronized (_dataListener) {
            listeners = _dataListener.get(path);
            if (listeners == null) {
                listeners = new CopyOnWriteArraySet<IZkDataListener>();
                _dataListener.put(path, listeners);
            }
            listeners.add(listener);
        }
        watchForData(path);
        logger.debug("Subscribed data changes for " + path);
    }

    public void unsubscribeDataChanges(String path, IZkDataListener dataListener) {
        synchronized (_dataListener) {
            final Set<IZkDataListener> listeners = _dataListener.get(path);
            if (listeners != null) {
                listeners.remove(dataListener);
            }
            if (listeners == null || listeners.isEmpty()) {
                _dataListener.remove(path);
            }
        }
    }

    public void subscribeStateChanges(final IZkStateListener listener) {
        synchronized (_stateListener) {
            _stateListener.add(listener);
        }
    }

    public void unsubscribeStateChanges(IZkStateListener stateListener) {
        synchronized (_stateListener) {
            _stateListener.remove(stateListener);
        }
    }

    public void unsubscribeAll() {
        synchronized (_childListener) {
            _childListener.clear();
        }
        synchronized (_dataListener) {
            _dataListener.clear();
        }
        synchronized (_stateListener) {
            _stateListener.clear();
        }
    }

	@Override
	public void process(WatchedEvent event) {
		_zookeeperEventThread = Thread.currentThread();

		boolean stateChanged = event.getPath() == null;
		boolean znodeChanged = event.getPath() != null;
		boolean dataChanged = event.getType() == EventType.NodeDataChanged
				|| event.getType() == EventType.NodeDeleted
				|| event.getType() == EventType.NodeCreated
				|| event.getType() == EventType.NodeChildrenChanged;

		getEventLock().lock();
		try {

			// We might have to install child change event listener if a new
			// node was created
			if (getShutdownTrigger()) {
                logger.debug("ignoring event '{" + event.getType() + " | " + event.getPath() + "}' since shutdown triggered");
                return;
            }
			if (stateChanged) {
				processStateChanged(event);
			}
			if (dataChanged) {
				processDataOrChildChange(event);
			}
		} finally {
			if (stateChanged) {
				getEventLock().getStateChangedCondition().signalAll();

				// If the session expired we have to signal all conditions,
				// because watches might have been removed and
				// there is no guarantee that those
				// conditions will be signaled at all after an Expired event
				// TODO PVo write a test for this
				if (event.getState() == KeeperState.Expired) {
					getEventLock().getZNodeEventCondition().signalAll();
					getEventLock().getDataChangedCondition().signalAll();
					// We also have to notify all listeners that something might
					// have changed
					fireAllEvents();
				}
			}
			if (znodeChanged) {
				getEventLock().getZNodeEventCondition().signalAll();
			}
			if (dataChanged) {
				getEventLock().getDataChangedCondition().signalAll();
			}
			getEventLock().unlock();
			logger.debug("Leaving process event");
		}

	}

	private void fireAllEvents() {
		for (Entry<String, Set<IZkChildListener>> entry : _childListener
				.entrySet()) {
			fireChildChangedEvents(entry.getKey(), entry.getValue());
		}
		for (Entry<String, Set<IZkDataListener>> entry : _dataListener
				.entrySet()) {
			fireDataChangedEvents(entry.getKey(), entry.getValue());
		}
	}

	/*
	 * 
	 */
	private void processStateChanged(WatchedEvent event) {
		logger.info("zookeeper state changed (" + event.getState() + ")");
		setCurrentState(event.getState());
		if (getShutdownTrigger()) {
            return;
        }
		try {
			fireStateChangedEvent(event.getState());

			if (event.getState() == KeeperState.Expired) {
				reconnect();
				fireNewSessionEvents();
			}
		} catch (final Exception e) {
			throw new RuntimeException("Exception while restarting zk client",
					e);
		}
	}

	private void processDataOrChildChange(WatchedEvent event) {
		final String path = event.getPath();

		if (event.getType() == EventType.NodeChildrenChanged
				|| event.getType() == EventType.NodeCreated
				|| event.getType() == EventType.NodeDeleted) {
			Set<IZkChildListener> childListeners = _childListener.get(path);
			if (childListeners != null && !childListeners.isEmpty()) {
				fireChildChangedEvents(path, childListeners);
			}
		}

		if (event.getType() == EventType.NodeDataChanged
				|| event.getType() == EventType.NodeDeleted
				|| event.getType() == EventType.NodeCreated) {
			Set<IZkDataListener> listeners = _dataListener.get(path);
			if (listeners != null && !listeners.isEmpty()) {
				fireDataChangedEvents(event.getPath(), listeners);
			}
		}
	}

	private void fireDataChangedEvents(final String path,
			Set<IZkDataListener> listeners) {
		for (final IZkDataListener listener : listeners) {
			_eventThread.send(new ZkEventThread.ZkEvent("Data of " + path
					+ " changed sent to " + listener) {

				@Override
				public void run() throws Exception {
					// reinstall watch
					exists(path, true);
					try {
						Object data = readData(path, null, true);
						listener.handleDataChange(path, data);
					} catch (ZkNoNodeException e) {
						listener.handleDataDeleted(path);
					}
				}
			});
		}
	}

	protected byte[] readData(String path, Stat stat, boolean b)
			throws KeeperException, InterruptedException {
		return zookeeper.getData(path, b, stat);
	}
	
	public <T> T retryUntilConnected(Callable<T> callable) throws ZkInterruptedException, IllegalArgumentException, ZkException, RuntimeException {
        if (_zookeeperEventThread != null && Thread.currentThread() == _zookeeperEventThread) {
            throw new IllegalArgumentException("Must not be done in the zookeeper event thread.");
        }
        while (true) {
          if (_shutdownTriggered) throw new ZkInterruptedException(new InterruptedException());
            try {
                return callable.call();
            } catch (ConnectionLossException e) {
                // we give the event thread some time to update the status to 'Disconnected'
                Thread.yield();
                waitUntilConnected(99000,TimeUnit.SECONDS);
            } catch (SessionExpiredException e) {
                // we give the event thread some time to update the status to 'Expired'
                Thread.yield();
                waitUntilConnected(99000,TimeUnit.SECONDS);
            } catch (KeeperException e) {
                throw ZkException.create(e);
            } catch (InterruptedException e) {
                throw new ZkInterruptedException(e);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }
    }

	/*
	 * 出错情况下也返回
	 */
	public byte[] readData(String path, boolean returnZeroIfPathNotExists) {
		byte[] data = new byte[] { 0 };
		try {
			data = readData(path, null, false);
		} catch (ZkNoNodeException e) {
			if (!returnZeroIfPathNotExists) {
				throw e;
			}
		} catch (KeeperException e) {
			logger.error(e.getMessage(), e);
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
		}
		return data;
	}

	/*
	 * 
	 */
	protected boolean exists(final String path, final boolean watch)
			throws KeeperException, InterruptedException {
		return retryUntilConnected(new Callable<Boolean>(){
			@Override
			public Boolean call() throws Exception {
				return zookeeper.exists(path, watch) != null;
			}
			
		});
		
	}
	
	/*
	 * 
	 * 立即返回的exist函数。。 
	 */
	public boolean existsWithoutDelay(String path) throws KeeperException, InterruptedException{
		return zookeeper.exists(path, false) != null;
	}

	public boolean exists(final String path) throws KeeperException,
			InterruptedException {
		return exists(path, hasListeners(path));
	}

	public String create(final String path, byte[] data, final CreateMode mode)
			throws ZkInterruptedException, IllegalArgumentException,
			ZkException, RuntimeException, KeeperException,
			InterruptedException {
		if (path == null) {
			throw new NullPointerException("path must not be null.");
		}
		// final byte[] bytes = data;
		String result = null;
		try {
			result = zookeeper.create(path, data, Ids.OPEN_ACL_UNSAFE, mode);
		}catch (KeeperException e) {
            throw ZkException.create(e);
        } catch (InterruptedException e) {
            throw new ZkInterruptedException(e);
        } catch (Exception e) {
            throw ExceptionUtil.convertToRuntimeException(e);
        }
		return result;
	}

	public void createPersistent(String path) throws ZkInterruptedException,
			IllegalArgumentException, ZkException, RuntimeException,
			KeeperException, InterruptedException {
		createPersistent(path, false);
	}

	// 是否创建父节点
	public void createPersistent(String path, boolean createParents)
			throws ZkInterruptedException, IllegalArgumentException,
			ZkException, RuntimeException, KeeperException,
			InterruptedException {
		try {
			create(path, null, CreateMode.PERSISTENT);
		} catch (ZkNodeExistsException e) {
			if (!createParents) {
				throw e;
			}
		} catch (ZkNoNodeException e) {
			if (!createParents) {
				throw e;
			}
			String parentDir = path.substring(0, path.lastIndexOf('/'));
			createPersistent(parentDir, createParents);
			createPersistent(path, createParents);
		}
	}

	public void createPersistent(String path, byte[] data)
			throws ZkInterruptedException, IllegalArgumentException,
			ZkException, RuntimeException, KeeperException,
			InterruptedException {
		create(path, data, CreateMode.PERSISTENT);
	}

	public void createEphemeral(final String path)
			throws ZkInterruptedException, IllegalArgumentException,
			ZkException, RuntimeException, KeeperException,
			InterruptedException {
		create(path, null, CreateMode.EPHEMERAL);
	}
	
	public void createEphemeral(String path, byte[] data) throws ZkInterruptedException, IllegalArgumentException, ZkException, RuntimeException, KeeperException, InterruptedException {
		create(path, data, CreateMode.EPHEMERAL);
		
	}

	public String createPersistentSequential(String path, byte[] data)
			throws ZkInterruptedException, IllegalArgumentException,
			ZkException, RuntimeException, KeeperException,
			InterruptedException {
		return create(path, data, CreateMode.PERSISTENT_SEQUENTIAL);
	}

	public void delete(String path) throws InterruptedException,
			KeeperException {
		zookeeper.delete(path, -1);
	}

	private boolean hasListeners(String path) {
		Set<IZkDataListener> dataListeners = _dataListener.get(path);
		if (dataListeners != null && dataListeners.size() > 0) {
			return true;
		}
		Set<IZkChildListener> childListeners = _childListener.get(path);
		if (childListeners != null && childListeners.size() > 0) {
			return true;
		}
		return false;
	}

	public List<String> getChildren(String path) throws KeeperException,
			InterruptedException {
		return getChildren(path, hasListeners(path));
	}

	public List<String> getChildren(final String path, final boolean watch)
			throws KeeperException, InterruptedException {
		return zookeeper.getChildren(path, watch);
	}

	public void writeData(String path, byte[] object) throws KeeperException,
			InterruptedException {
		writeData(path, object, -1);
	}

	public void writeData(final String path, byte[] datat,
			final int expectedVersion) throws KeeperException,
			InterruptedException {
		zookeeper.setData(path, datat, expectedVersion);
	}

	public void watchForData(final String path) {
        retryUntilConnected(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                zookeeper.exists(path, true);
                return null;
            }
        });
    }
	
	/*
	 * 取得version
	 */
	public Stat getVersion(final String path){
		Stat stat = retryUntilConnected(new Callable<Stat>() {
            @Override
            public Stat call() throws Exception {
                zookeeper.exists(path, true);
                Stat stat = zookeeper.exists(path, false);
                return stat;
            }
        });
		return stat;
	}
	

    /**
     * Installs a child watch for the given path.
     * 
     * @param path
     * @return the current children of the path or null if the zk node with the given path doesn't exist.
     */
    public List<String> watchForChilds(final String path,final boolean isWatch) {
        if (_zookeeperEventThread != null && Thread.currentThread() == _zookeeperEventThread) {
            throw new IllegalArgumentException("Must not be done in the zookeeper event thread.");
        }
        return retryUntilConnected(new Callable<List<String>>() {
            @Override
            public List<String> call() throws Exception {
                exists(path, isWatch);
                try {
                    return getChildren(path, isWatch);
                } catch (ZkNoNodeException e) {
                    // ignore, the "exists" watch will listen for the parent node to appear
                }
                return null;
            }
        });
    }

	private void fireChildChangedEvents(final String path,
			Set<IZkChildListener> childListeners) {
		try {
			// reinstall the watch
			for (final IZkChildListener listener : childListeners) {
				_eventThread.send(new ZkEventThread.ZkEvent("Children of " + path
						+ " changed sent to " + listener) {

					@Override
					public void run() throws Exception {
						try {
							// if the node doesn't exist we should listen for
							// the root node to reappear
							//此处为同步调用，不能做异步话处理
							
							exists(path);
							List<String> children = getChildren(path);
							listener.handleChildChange(path, children);
						} catch (ZkNoNodeException e) {
							listener.handleChildChange(path, null);
						}
					}
				});
			}
		} catch (Exception e) {
			logger.error(
					"Failed to fire child changed event. Unable to getChildren.  ",
					e);
		}
	}

	private void fireNewSessionEvents() {
		for (final IZkStateListener stateListener : _stateListener) {
			_eventThread.send(new ZkEventThread.ZkEvent("New session event sent to "
					+ stateListener) {

				@Override
				public void run() throws Exception {
					stateListener.handleNewSession();
				}
			});
		}
	}

	private void fireStateChangedEvent(final KeeperState state) {
		for (final IZkStateListener stateListener : _stateListener) {
			_eventThread.send(new ZkEventThread.ZkEvent("State changed to " + state
					+ " sent to " + stateListener) {

				@Override
				public void run() throws Exception {
					stateListener.handleStateChanged(state);
				}
			});
		}
	}

	public boolean waitUntilConnected(long time, TimeUnit timeUnit)
			throws ZkInterruptedException {
		return waitForKeeperState(KeeperState.SyncConnected, time, timeUnit);
	}

	public boolean waitForKeeperState(KeeperState keeperState, long time,
			TimeUnit timeUnit) {

		Date timeout = new Date(System.currentTimeMillis()
				+ timeUnit.toMillis(time));

		logger.debug("Waiting for keeper state " + keeperState);
		acquireEventLock();
		try {
			boolean stillWaiting = true;
			while (_currentState != keeperState) {
				if (!stillWaiting) {
					return false;
				}
				stillWaiting = getEventLock().getStateChangedCondition()
						.awaitUntil(timeout);
			}
			logger.debug("State is " + _currentState);
			return true;
		} catch (InterruptedException e) {
			throw new RuntimeException("error when conn");
		} finally {
			getEventLock().unlock();
		}
	}

	public ZooKeeper getZookeeper() {
		return zookeeper;
	}

	public ZkLock getEventLock() {
		return _zkEventLock;
	}

	private void acquireEventLock() {
		try {
			getEventLock().lockInterruptibly();
		} catch (InterruptedException e) {
			throw new ZkInterruptedException(e);
		}
	}

	public void setCurrentState(KeeperState currentState) {
		getEventLock().lock();
		try {
			_currentState = currentState;
		} finally {
			getEventLock().unlock();
		}
	}

	private void reconnect() {
		
		logger.info("reconnect to zookeeper client,keeperState:{}",this._currentState);
		getEventLock().lock();
		try {
			zkClose();
			zkConnect(this);
		} catch (InterruptedException e) {
			throw new ZkInterruptedException(e);
		} finally {
			getEventLock().unlock();
		}
	}

	/*
	 * close zookeeper
	 */
	public void close() {

		logger.warn("Closing ZkClient:{} timeout:{}",this._connStr,this._connectionTimeout);
		getEventLock().lock();
		try {
            try {
                setShutdownTrigger(true);
                _eventThread.interrupt();
                _eventThread.join(2000);
            } catch (InterruptedException e1) {
                logger.error("_eventThread.interupt() InterruptException...",e1);
            }

			zkClose();
		} catch (InterruptedException e) {
			throw new ZkInterruptedException(e);
		} finally {
			getEventLock().unlock();
		}
		logger.warn("Closing ZkClient done connStr:{} timeout:{}",this._connStr,this._connectionTimeout);

	}
	
	
	private void zkConnect(Watcher watcher) {
        _zookeeperLock.lock();
        try {
            if (zookeeper != null) {
                throw new IllegalStateException("zk client has already been started");
            }
            try {
                logger.debug("Creating new ZookKeeper instance to connect to " + this._connStr + ".");
                zookeeper = new ZooKeeper(_connStr, this._sessionTimeout, watcher);
            } catch (IOException e) {
                throw new ZkException("Unable to connect to " + _connStr, e);
            }
        } finally {
            _zookeeperLock.unlock();
        }
    }

    private void zkClose() throws InterruptedException {
        _zookeeperLock.lock();
        try {
            if (zookeeper != null) {
                logger.debug("Closing ZooKeeper connected to " + this._connStr);
                zookeeper.close();
                zookeeper = null;
            }
        } finally {
            _zookeeperLock.unlock();
        }
    }

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			ZkClient client = new ZkClient("192.168.229.53:2183", 3000, 300000);
			client.connect();
			//client.create("/1test", null, CreateMode.PERSISTENT);
			Boolean s1 = client.exists("/1test");
			System.out.println("/1test:" + s1);
			List<String> strs = client.getChildren("/saf_test/mytest", true);
			System.out.println("/saf_test/mytest:"+strs.size());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeeperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * @return the _connStr
	 */
	public String getConnectString() {
		return _connStr;
	}
	
	public KeeperState getCurrentState(){
		return this._currentState;
	}
	
	
	public void setShutdownTrigger(boolean triggerState) {
        _shutdownTriggered = triggerState;
    }

    public boolean getShutdownTrigger() {
        return _shutdownTriggered;
    }

	
	
	public boolean isAvailable() {
		if(zookeeper == null) return false;
		return _currentState == KeeperState.SyncConnected
				|| _currentState == KeeperState.ConnectedReadOnly;
	}
	
}

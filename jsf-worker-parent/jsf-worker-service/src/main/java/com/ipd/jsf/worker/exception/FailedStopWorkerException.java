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
package com.ipd.jsf.worker.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FailedStopWorkerException extends RuntimeException {

    private static final long serialVersionUID = -7523600657827285524L;

    private static Logger logger = LoggerFactory.getLogger(FailedStopWorkerException.class);

    public FailedStopWorkerException() {
        super();
    }

    public FailedStopWorkerException(String message) {
        super(messageHandler(message));
        logger.error(message);
    }

    public FailedStopWorkerException(String message, Throwable cause) {
        super(messageHandler(message), cause);
        logger.error(message);
        cause.printStackTrace();
    }

    public FailedStopWorkerException(Throwable cause) {
        super(cause);
        cause.printStackTrace();
    }

    private static String messageHandler(String message) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(message);
        return stringBuffer.toString();
    }
}

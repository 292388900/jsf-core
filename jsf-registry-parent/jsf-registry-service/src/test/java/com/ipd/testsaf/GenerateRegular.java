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
package com.ipd.testsaf;

public class GenerateRegular {

    /**
     * @param args
     */
    public static void main(String[] args) {
        int t1 = 1;
        int t2 = 255;
        long start = System.currentTimeMillis();
        GenerateRegular t = new GenerateRegular();
        System.out.println(t.getPointRegular(t1, t2) + ", elapse:" + (System.currentTimeMillis() - start));
//        System.out.println(t.getMeddle(122, t.getUpLimit(122), 188));
    }

    public String getPointRegular(int first, int second) {
        String rule = "";
        if (second >= getUpLimit(first)) {
            rule = getEdgeArea(first, getUpLimit(first));
            if ((getNextUpLimit(first)) <= second) {
                rule = rule + "|" + getMeddle(getNextDownLimit(first), getNextUpLimit(first), second);
                if (second < getUpLimit(second)) {   //如果second小于当前范围的最大值，就继续获取rule
                    if ((getNextUpLimit(first)) < second) {
                        rule = rule + "|" + getEdgeArea(getDownLimit(second), second);
                    }
                }
            } else {
                rule = rule + "|" + getEdgeArea(getDownLimit(second), second);
            }
        } else {
            rule = getEdgeArea(first, second);
        }
        
        return rule;
    }

    public String getEdgeArea(int first, int second) {
        String result = "";
        if (first == second) {
            result = String.valueOf(first);
        } else {
            if (getScale(first) == 1) {
                result = "[" + first + "-" + second + "]";
            } else if (getScale(first) == 2) {
                result = first / 10 + "[" + first % 10 + "-" + second % 10 + "]";
            } else if (getScale(first) == 3) {
                result = "1" + (first - 100) / 10 + "[" + (first - 100) % 10 + "-"  + (second - 100) % 10 + "]";
            } else if (getScale(first) == 4) {
                result = "2" + (first - 200) / 10 + "[" + (first - 100) % 10 + "-"  + (second - 100) % 10 + "]";
            }
        }
        return result;
    }

    /**
     * 取中间范围数据的正则
     * @param first
     * @param second
     * @param target
     * @return
     */
    public String getMeddle (int first, int second, int target) {
        int nextSecond = second;
        while (nextSecond < target && getScaleMaxNum(nextSecond) > nextSecond) {
            int next = getNextUpLimit(nextSecond);
            if (next <= target) {
                nextSecond = next;
            } else {
                break;
            }
        }
        String result = "";
        if (getScale(first) == 1) {
            result = "[" + first + "-" + nextSecond + "]";
        } else if (getScale(first) == 2) {
            if (first / 10 == nextSecond / 10) {
                result = first / 10 + "[0-9]";
            } else {
                result = "[" + first / 10 + "-" + nextSecond / 10 + "][0-9]";
            }
        } else if (getScale(first) == 3) {
            if ((first - 100) / 10 == (nextSecond -100) / 10) {
                result = "1" + (first - 100) / 10 + "[0-9]";
            } else {
                result = "1[" + (first - 100) / 10 + "-" + (nextSecond - 100) / 10 + "][0-9]";
            }
        } else if (getScale(first) == 4) {
            if ((first - 200) / 10 == (nextSecond -200) / 10) {
                result = "2" + (first - 200) / 10 + "[0-9]";
            } else {
                result = "2[" + (first - 200) / 10 + "-" + (nextSecond - 200) / 10 + "][0-9]";
            }
        }
        if (getNextUpLimit(nextSecond) < target && !result.equals("")) {
            result = result + "|" +getMeddle(getNextDownLimit(nextSecond), getNextUpLimit(nextSecond), target);
        }
        return result;
    }

    public int getUpLimit(int t) {
        int result = 0;
        if (getScale(t) == 1) {
            result = 9;
        } else {
            result = t / 10 * 10 + 9;
        } 
        return result;
    }
    
    public int getDownLimit(int t) {
        int result = 0;
        if (getScale(t) == 1) {
            result = 0;
        } else {
            result = t / 10 * 10;
        } 
        return result;
    }

    /**
     * 跨度是10
     * @param t
     * @return
     */
    public int getNextUpLimit(int t) {
        int result = 0;
        result = (t + 10) / 10 * 10 + 9;
        return result;
    }
    
    
    /**
     * 跨度是10
     * @param t
     * @return
     */
    public int getNextDownLimit(int t) {
        int result = 0;
        result = (t + 10)/ 10 * 10;
        return result;
    }
    
    public int getScale(int i) {
        if (i >= 0 && i <= 9) {
            return 1;
        } else if (i >= 10 && i <= 99) {
            return 2;
        } else if (i >= 100 && i <= 199) {
            return 3;
        }
        return 4;
    }

    public int getScaleMaxNum(int i) {
        if (getScale(i) == 1) {
            return 9;
        } else if (getScale(i) == 2) {
            return 99;
        } else if (getScale(i) == 3) {
            return 199;
        }
        return 255;
    }
}

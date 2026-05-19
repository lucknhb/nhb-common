package com.nhb.common.id.buffer;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/5/18 15:56
 * @description: 当消费者（Consumer）尝试从缓冲区获取数据（ID）时，如果头指针（cursor）追上了尾指针（tail），说明缓冲区中已经没有可用数据了。<BR/>
 * 此时，根据业务需求，系统可以选择抛出异常、阻塞等待或执行其他降级逻辑
 */
@FunctionalInterface
public interface RejectedTakeBufferHandler {
    /**
     * 拒绝从缓冲区获取数据的处理逻辑
     *
     * @param ringBuffer 当前发生拒绝操作的环形缓冲区实例
     */
    void rejectTakeBuffer(RingBuffer ringBuffer);
}

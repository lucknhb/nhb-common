package com.nhb.common.id.buffer;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/5/18 15:55
 * @description: 当生产者（Producer）尝试向缓冲区放入数据（ID）时，如果缓冲区的尾指针（tail）追上了头指针（cursor），说明缓冲区已满。<BR/>
 * 此时，系统不能无限阻塞生产者（否则会导致线程池耗尽或响应时间变长），而是会调用此接口定义的方法来处理这个“被拒绝”的请求
 */
@FunctionalInterface
public interface RejectedPutBufferHandler {

    /**
     * 拒绝放入缓冲区的处理逻辑
     *
     * @param ringBuffer 当前发生拒绝操作的环形缓冲区实例
     * @param id        待放入但被拒绝的 ID 数据
     */
    void rejectPutBuffer(RingBuffer ringBuffer, long id);
}

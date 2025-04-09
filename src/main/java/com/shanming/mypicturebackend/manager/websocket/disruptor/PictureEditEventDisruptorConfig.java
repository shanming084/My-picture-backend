package com.shanming.mypicturebackend.manager.websocket.disruptor;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.lmax.disruptor.dsl.Disruptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * Disruptor 配置类，将我们刚定义的事件及处理器关联到 Disruptor 实例中：
 */
@Configuration
public class PictureEditEventDisruptorConfig {


    @Resource
    private PictureEditEventWorkHandler pictureEditEventWorkHandler;

    //定义名字，便于使用
    @Bean("pictureEditEventDisruptor")
    public Disruptor<PictureEditEvent> messageModelRingBuffer() {
        // ringBuffer 的大小
        int bufferSize = 1024 * 256;
        //创建disruptor
        Disruptor<PictureEditEvent> disruptor = new Disruptor<>(
                //环形队列的数据类型-即图片编辑操作体
                PictureEditEvent::new,
                //环形队列大小
                bufferSize,
                //线程工厂
                ThreadFactoryBuilder.create().setNamePrefix("pictureEditEventDisruptor").build()
        );
        // 设置消费者
        disruptor.handleEventsWithWorkerPool(pictureEditEventWorkHandler);
        // 开启 disruptor
        disruptor.start();
        return disruptor;
    }
}

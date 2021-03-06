package cn.itcast.core.listener;

import cn.itcast.core.service.itemSearch.ItemSearchService;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

//自定义消息监听器--------将商品保存到索引库
public class ItemSearchListener implements MessageListener {

    @Resource
    private ItemSearchService itemSearchService;

    /**
     * 获取mq中的消息
     * @param message
     */
    @Override
    public void onMessage(Message message) {
        try {
            //获取消息
            ActiveMQTextMessage activeMQTextMessage = (ActiveMQTextMessage) message;
            String id = activeMQTextMessage.getText();
            System.out.println("消费者service_search获取到的id"+id);
            //消费消息----------------处理业务
            itemSearchService.updateSolr(Long.parseLong(id));
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}

package org.craftgalaxy.galaxycore.client.chat;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("rawtypes")
public class ChannelPipeline<T> {

    private final Map<Class<? extends AbstractChatHandler>, AbstractChatHandler<T>> pipeline = new HashMap<>();
    private AbstractChatHandler<T> head;
    private AbstractChatHandler<T> tail;

    public ChannelPipeline<T> addLast(AbstractChatHandler<T> handler) {
        if (this.tail == null) {
            if (this.head == null) {
                this.head = handler;
            } else {
                AbstractChatHandler<T> next = this.head.getContext().getNext();
                while (next.getContext().getNext() != null) {
                    next = next.getContext().getNext();
                }

                next.getContext().setNext(handler);
                handler.getContext().setPrevious(next);
            }
        } else {
            this.tail.getContext().setNext(handler);
            handler.getContext().setPrevious(this.tail);
        }

        this.tail = handler;
        this.pipeline.put(handler.getClass(), handler);
        return this;
    }

    public ChannelPipeline<T> addFirst(AbstractChatHandler<T> handler) {
        if (this.head == null) {
            if (this.tail == null) {
                this.tail = handler;
            } else {
                AbstractChatHandler<T> previous = this.tail.getContext().getPrevious();
                while (previous.getContext().getPrevious() != null) {
                    previous = previous.getContext().getPrevious();
                }

                previous.getContext().setPrevious(handler);
                handler.getContext().setNext(previous);
            }
        } else {
            this.head.getContext().setPrevious(handler);
            handler.getContext().setNext(this.head);
        }

        this.head = handler;
        this.pipeline.put(handler.getClass(), handler);
        return this;
    }

    public ChannelPipeline<T> addBefore(Class<? extends AbstractChatHandler<T>> clazz, AbstractChatHandler<T> handler) {
        AbstractChatHandler<T> oldAfter = this.pipeline.get(clazz);
        if (oldAfter != null) {
            AbstractChatHandler<T> oldPrevious = oldAfter.getContext().getPrevious();
            oldPrevious.getContext().setNext(handler);
            oldAfter.getContext().setPrevious(handler);
            this.pipeline.put(handler.getClass(), handler);
        }

        return this;
    }

    public AbstractChatHandler<T> getHead() {
        return this.head;
    }

    public AbstractChatHandler<T> getTail() {
        return this.tail;
    }
}

package com.zetaplugins.lifestealz.util.scheduler;

class CraftAsyncDebugger {
    private CraftAsyncDebugger next = null;
    private final int expiry;
    private final Class<?> clazz;

    CraftAsyncDebugger(final int expiry, final Class<?> clazz) {
        this.expiry = expiry;
        this.clazz = clazz;

    }

    final CraftAsyncDebugger getNextHead(final int time) {
        CraftAsyncDebugger next, current = this;
        while (time > current.expiry && (next = current.next) != null) {
            current = next;
        }
        return current;
    }

    final CraftAsyncDebugger setNext(final CraftAsyncDebugger next) {
        return this.next = next;
    }

    StringBuilder debugTo(final StringBuilder string) {
        for (CraftAsyncDebugger next = this; next != null; next = next.next) {
            string.append("LifeStealZ:").append(next.clazz.getName()).append('@').append(next.expiry).append(',');
        }
        return string;
    }
}
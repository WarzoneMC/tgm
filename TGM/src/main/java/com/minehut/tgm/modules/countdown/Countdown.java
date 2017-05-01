package com.minehut.tgm.modules.countdown;

import com.minehut.tgm.match.MatchModule;
import com.minehut.tgm.modules.tasked.TaskedModule;
import lombok.Getter;
import lombok.Setter;

public abstract class Countdown extends MatchModule implements TaskedModule {
    @Getter @Setter private boolean cancelled = true;

    @Getter @Setter private double timeLeft, timeMax; //ticks


    public void start(double countdown) {
        setCancelled(false);

        setTimeMax(countdown * 20);
        setTimeLeft(countdown * 20);

        onStart();
    }

    public void cancel() {
        setCancelled(true);
        onCancel();
    }

    @Override
    public void tick() {
        if (isCancelled()) return;

        if (timeLeft <= 0) {
            onFinish();
            setCancelled(true);
        } else {
            onTick();
            timeLeft--;
        }
    }

    public int getTimeLeftSeconds() {
        return (int) timeLeft / 20;
    }

    public double getTimeMaxSeconds() {
        return timeMax / 20;
    }

    protected abstract void onStart();
    protected abstract void onTick();
    protected abstract void onFinish();
    protected abstract void onCancel();
}

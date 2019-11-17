package network.warzone.tgm.modules.countdown;

import lombok.Getter;
import lombok.Setter;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.match.ModuleData;
import network.warzone.tgm.match.ModuleLoadTime;
import network.warzone.tgm.modules.tasked.TaskedModule;

@ModuleData(load = ModuleLoadTime.LATER)
@Getter @Setter
public abstract class Countdown extends MatchModule implements TaskedModule {

    private double timeLeft, timeMax; //ticks

    private boolean cancelled = true;


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

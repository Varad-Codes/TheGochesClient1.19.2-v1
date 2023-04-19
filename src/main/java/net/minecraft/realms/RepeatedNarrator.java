package net.minecraft.realms;

import com.google.common.util.concurrent.RateLimiter;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.client.GameNarrator;
import net.minecraft.network.chat.Component;

public class RepeatedNarrator
{
    private final float permitsPerSecond;
    private final AtomicReference<RepeatedNarrator.Params> params = new AtomicReference<>();

    public RepeatedNarrator(Duration pDuration)
    {
        this.permitsPerSecond = 1000.0F / (float)pDuration.toMillis();
    }

    public void narrate(GameNarrator p_240528_, Component p_240604_)
    {
        RepeatedNarrator.Params repeatednarrator$params = this.params.updateAndGet((p_175080_) ->
        {
            return p_175080_ != null && p_240604_.equals(p_175080_.narration) ? p_175080_ : new RepeatedNarrator.Params(p_240604_, RateLimiter.create((double)this.permitsPerSecond));
        });

        if (repeatednarrator$params.rateLimiter.tryAcquire(1))
        {
            p_240528_.sayNow(p_240604_);
        }
    }

    static class Params
    {
        final Component narration;
        final RateLimiter rateLimiter;

        Params(Component pNarration, RateLimiter pRateLimiter)
        {
            this.narration = pNarration;
            this.rateLimiter = pRateLimiter;
        }
    }
}

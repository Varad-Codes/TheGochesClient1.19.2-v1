package net.minecraft.client;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;
import net.minecraft.client.gui.components.AbstractOptionSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.TooltipAccessor;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.OptionEnum;
import net.optifine.config.FloatOptions;
import net.optifine.config.SliderPercentageOptionOF;
import net.optifine.config.SliderableValueSetInt;
import net.optifine.gui.IOptionControl;
import org.slf4j.Logger;

public class OptionInstance<T>
{
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final OptionInstance.Enum<Boolean> BOOLEAN_VALUES = new OptionInstance.Enum<>(ImmutableList.of(Boolean.TRUE, Boolean.FALSE), Codec.BOOL);
    private static final int TOOLTIP_WIDTH = 200;
    private final OptionInstance.TooltipSupplierFactory<T> tooltip;
    final Function<T, Component> toString;
    private final OptionInstance.ValueSet<T> values;
    private final Codec<T> codec;
    private final T initialValue;
    private final Consumer<T> onValueUpdate;
    final Component caption;
    T value;
    private String resourceKey;
    public static final Map<String, OptionInstance> OPTIONS_BY_KEY = new HashMap<>();

    public static OptionInstance<Boolean> createBoolean(String p_231529_, boolean p_231530_, Consumer<Boolean> p_231531_)
    {
        return createBoolean(p_231529_, noTooltip(), p_231530_, p_231531_);
    }

    public static OptionInstance<Boolean> createBoolean(String p_231526_, boolean p_231527_)
    {
        return createBoolean(p_231526_, noTooltip(), p_231527_, (p_231547_0_) ->
        {
        });
    }

    public static OptionInstance<Boolean> createBoolean(String p_231517_, OptionInstance.TooltipSupplierFactory<Boolean> p_231518_, boolean p_231519_)
    {
        return createBoolean(p_231517_, p_231518_, p_231519_, (p_231512_0_) ->
        {
        });
    }

    public static OptionInstance<Boolean> createBoolean(String p_231521_, OptionInstance.TooltipSupplierFactory<Boolean> p_231522_, boolean p_231523_, Consumer<Boolean> p_231524_)
    {
        return new OptionInstance<>(p_231521_, p_231522_, (p_231543_0_, p_231543_1_) ->
        {
            return p_231543_1_ ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF;
        }, BOOLEAN_VALUES, p_231523_, p_231524_);
    }

    public OptionInstance(String p_231492_, OptionInstance.TooltipSupplierFactory<T> p_231493_, OptionInstance.CaptionBasedToString<T> p_231494_, OptionInstance.ValueSet<T> p_231495_, T p_231496_, Consumer<T> p_231497_)
    {
        this(p_231492_, p_231493_, p_231494_, p_231495_, p_231495_.codec(), p_231496_, p_231497_);
    }

    public OptionInstance(String p_231484_, OptionInstance.TooltipSupplierFactory<T> p_231485_, OptionInstance.CaptionBasedToString<T> p_231486_, OptionInstance.ValueSet<T> p_231487_, Codec<T> p_231488_, T p_231489_, Consumer<T> p_231490_)
    {
        this.caption = Component.translatable(p_231484_);
        this.tooltip = p_231485_;
        this.toString = (p_231504_2_) ->
        {
            return p_231486_.toString(this.caption, p_231504_2_);
        };
        this.values = p_231487_;
        this.codec = p_231488_;
        this.initialValue = p_231489_;
        this.onValueUpdate = p_231490_;
        this.value = this.initialValue;
        this.resourceKey = p_231484_;
        OPTIONS_BY_KEY.put(this.resourceKey, this);
    }

    public static <T> OptionInstance.TooltipSupplierFactory<T> noTooltip()
    {
        return (p_231499_0_) ->
        {
            return (p_231552_0_) -> {
                return ImmutableList.of();
            };
        };
    }

    public static <T> OptionInstance.TooltipSupplierFactory<T> cachedConstantTooltip(Component p_231536_)
    {
        return (p_231540_1_) ->
        {
            List<FormattedCharSequence> list = splitTooltip(p_231540_1_, p_231536_);
            return (p_231532_1_) -> {
                return list;
            };
        };
    }

    public static <T extends OptionEnum> OptionInstance.CaptionBasedToString<T> forOptionEnum()
    {
        return (p_231537_0_, p_231537_1_) ->
        {
            return p_231537_1_.getCaption();
        };
    }

    protected static List<FormattedCharSequence> splitTooltip(Minecraft p_231502_, Component p_231503_)
    {
        return p_231502_.font.split(p_231503_, 200);
    }

    public AbstractWidget createButton(Options p_231508_, int p_231509_, int p_231510_, int p_231511_)
    {
        OptionInstance.TooltipSupplier<T> tooltipsupplier = this.tooltip.apply(Minecraft.getInstance());
        return this.values.createButton(tooltipsupplier, p_231508_, p_231509_, p_231510_, p_231511_).apply(this);
    }

    public T get()
    {
        if (this instanceof SliderPercentageOptionOF sliderpercentageoptionof)
        {
            if (this.value instanceof Integer)
            {
                return (T)(Integer)(int)sliderpercentageoptionof.getOptionValue();
            }

            if (this.value instanceof Double)
            {
                return (T)(Double)sliderpercentageoptionof.getOptionValue();
            }
        }

        return this.value;
    }

    public Codec<T> codec()
    {
        return this.codec;
    }

    public String toString()
    {
        return this.caption.getString();
    }

    public void set(T p_231515_)
    {
        T t = this.values.validateValue(p_231515_).orElseGet(() ->
        {
            LOGGER.error("Illegal option value " + p_231515_ + " for " + this.caption);
            return this.initialValue;
        });

        if (!Minecraft.getInstance().isRunning())
        {
            this.value = t;
        }
        else if (!Objects.equals(this.value, t))
        {
            this.value = t;
            this.onValueUpdate.accept(this.value);
        }
    }

    public OptionInstance.ValueSet<T> values()
    {
        return this.values;
    }

    public String getResourceKey()
    {
        return this.resourceKey;
    }

    public Component getCaption()
    {
        return this.caption;
    }

    public T getMinValue()
    {
        OptionInstance.IntRangeBase optioninstance$intrangebase = this.getIntRangeBase();

        if (optioninstance$intrangebase != null)
        {
            return (T)(Integer)optioninstance$intrangebase.minInclusive();
        }
        else
        {
            throw new IllegalArgumentException("Min value not supported: " + this.getResourceKey());
        }
    }

    public T getMaxValue()
    {
        OptionInstance.IntRangeBase optioninstance$intrangebase = this.getIntRangeBase();

        if (optioninstance$intrangebase != null)
        {
            return (T)(Integer)optioninstance$intrangebase.maxInclusive();
        }
        else
        {
            throw new IllegalArgumentException("Max value not supported: " + this.getResourceKey());
        }
    }

    public OptionInstance.IntRangeBase getIntRangeBase()
    {
        if (this.values instanceof OptionInstance.IntRangeBase)
        {
            OptionInstance.IntRangeBase optioninstance$intrangebase = (OptionInstance.IntRangeBase)this.values;
            return optioninstance$intrangebase;
        }
        else
        {
            return this.values instanceof SliderableValueSetInt ? ((SliderableValueSetInt)this.values).getIntRange() : null;
        }
    }

    public boolean isProgressOption()
    {
        return this.values instanceof OptionInstance.SliderableValueSet;
    }

    public static record AltEnum<T>(List<T> values, List<T> altValues, BooleanSupplier altCondition, OptionInstance.CycleableValueSet.ValueSetter<T> valueSetter, Codec<T> codec) implements OptionInstance.CycleableValueSet<T>
    {
        public CycleButton.ValueListSupplier<T> valueListSupplier()
        {
            return CycleButton.ValueListSupplier.create(this.altCondition, this.values, this.altValues);
        }

        public Optional<T> validateValue(T p_231570_)
        {
            return (this.altCondition.getAsBoolean() ? this.altValues : this.values).contains(p_231570_) ? Optional.of(p_231570_) : Optional.empty();
        }
    }

    public interface CaptionBasedToString<T>
    {
        Component toString(Component p_231581_, T p_231582_);
    }

    public static record ClampingLazyMaxIntRange(int minInclusive, IntSupplier maxSupplier) implements OptionInstance.IntRangeBase, OptionInstance.SliderableOrCyclableValueSet<Integer>
    {
        public Optional<Integer> validateValue(Integer p_231590_)
        {
            return Optional.of(Mth.clamp(p_231590_, this.minInclusive(), this.maxInclusive()));
        }

        public int maxInclusive()
        {
            return this.maxSupplier.getAsInt();
        }

        public Codec<Integer> codec()
        {
            Function<Integer, DataResult<Integer>> function = (p_231595_1_) ->
            {
                int i = this.maxSupplier.getAsInt() + 1;
                return p_231595_1_.compareTo(this.minInclusive) >= 0 && p_231595_1_.compareTo(i) <= 0 ? DataResult.success(p_231595_1_) : DataResult.error("Value " + p_231595_1_ + " outside of range [" + this.minInclusive + ":" + i + "]", p_231595_1_);
            };
            return Codec.INT.flatXmap(function, function);
        }

        public boolean createCycleButton()
        {
            return true;
        }

        public CycleButton.ValueListSupplier<Integer> valueListSupplier()
        {
            return CycleButton.ValueListSupplier.create(IntStream.range(this.minInclusive, this.maxInclusive() + 1).boxed().toList());
        }

        public int minInclusive()
        {
            return this.minInclusive;
        }

        public IntSupplier maxSupplier()
        {
            return this.maxSupplier;
        }
    }

    interface CycleableValueSet<T> extends OptionInstance.ValueSet<T>
    {
        CycleButton.ValueListSupplier<T> valueListSupplier();

    default OptionInstance.CycleableValueSet.ValueSetter<T> valueSetter()
        {
            return OptionInstance::set;
        }

    default Function<OptionInstance<T>, AbstractWidget> createButton(OptionInstance.TooltipSupplier<T> p_231612_, Options p_231613_, int p_231614_, int p_231615_, int p_231616_)
        {
            return (p_231604_6_) ->
            {
                return CycleButton.builder(p_231604_6_.toString).withValues(this.valueListSupplier()).withTooltip(p_231612_).withInitialValue(p_231604_6_.value).create(p_231614_, p_231615_, p_231616_, 20, p_231604_6_.caption, (p_231617_3_, p_231617_4_) -> {
                    this.valueSetter().set(p_231604_6_, p_231617_4_);
                    p_231613_.save();
                });
            };
        }

        public interface ValueSetter<T>
        {
            void set(OptionInstance<T> p_231623_, T p_231624_);
        }
    }

    public static record Enum<T>(List<T> values, Codec<T> codec) implements OptionInstance.CycleableValueSet<T>
    {
        public Optional<T> validateValue(T p_231632_)
        {
            return this.values.contains(p_231632_) ? Optional.of(p_231632_) : Optional.empty();
        }

        public CycleButton.ValueListSupplier<T> valueListSupplier()
        {
            return CycleButton.ValueListSupplier.create(this.values);
        }
    }

    public static record IntRange(int minInclusive, int maxInclusive) implements OptionInstance.IntRangeBase
    {
        public Optional<Integer> validateValue(Integer p_231645_)
        {
            return p_231645_.compareTo(this.minInclusive()) >= 0 && p_231645_.compareTo(this.maxInclusive()) <= 0 ? Optional.of(p_231645_) : Optional.empty();
        }

        public Codec<Integer> codec()
        {
            return Codec.intRange(this.minInclusive, this.maxInclusive + 1);
        }

        public int minInclusive()
        {
            return this.minInclusive;
        }

        public int maxInclusive()
        {
            return this.maxInclusive;
        }
    }

    public interface IntRangeBase extends OptionInstance.SliderableValueSet<Integer>
    {
        int minInclusive();

        int maxInclusive();

    default double toSliderValue(Integer p_231663_)
        {
            return (double)Mth.map((float)p_231663_.intValue(), (float)this.minInclusive(), (float)this.maxInclusive(), 0.0F, 1.0F);
        }

    default Integer fromSliderValue(double p_231656_)
        {
            return Mth.floor(Mth.map(p_231656_, 0.0D, 1.0D, (double)this.minInclusive(), (double)this.maxInclusive()));
        }

    default <R> OptionInstance.SliderableValueSet<R> xmap(final IntFunction<? extends R> p_231658_, final ToIntFunction<? super R> p_231659_)
        {
            return new SliderableValueSetInt<R>()
            {
                public Optional<R> validateValue(R p_231674_)
                {
                    return IntRangeBase.this.validateValue(Integer.valueOf(p_231659_.applyAsInt(p_231674_))).map(p_231658_::apply);
                }
                public double toSliderValue(R p_231678_)
                {
                    return IntRangeBase.this.toSliderValue(p_231659_.applyAsInt(p_231678_));
                }
                public R fromSliderValue(double p_231676_)
                {
                    return p_231658_.apply(IntRangeBase.this.fromSliderValue(p_231676_));
                }
                public Codec<R> codec()
                {
                    return IntRangeBase.this.codec().xmap(p_231658_::apply, p_231659_::applyAsInt);
                }
                public OptionInstance.IntRangeBase getIntRange()
                {
                    return IntRangeBase.this;
                }
            };
        }
    }

    public static record LazyEnum<T>(Supplier<List<T>> values, Function<T, Optional<T>> validateValue, Codec<T> codec) implements OptionInstance.CycleableValueSet<T>
    {
        public Optional<T> validateValue(T p_231689_)
        {
            return this.validateValue.apply(p_231689_);
        }

        public CycleButton.ValueListSupplier<T> valueListSupplier()
        {
            return CycleButton.ValueListSupplier.create(this.values.get());
        }
    }

    static final class OptionInstanceSliderButton<N> extends AbstractOptionSliderButton implements TooltipAccessor, IOptionControl
    {
        private final OptionInstance<N> instance;
        private final OptionInstance.SliderableValueSet<N> values;
        private final OptionInstance.TooltipSupplier<N> tooltip;
        private boolean supportAdjusting;
        private boolean adjusting;

        OptionInstanceSliderButton(Options p_231701_, int p_231702_, int p_231703_, int p_231704_, int p_231705_, OptionInstance<N> p_231706_, OptionInstance.SliderableValueSet<N> p_231707_, OptionInstance.TooltipSupplier<N> p_231708_)
        {
            super(p_231701_, p_231702_, p_231703_, p_231704_, p_231705_, p_231707_.toSliderValue(p_231706_.get()));
            this.instance = p_231706_;
            this.values = p_231707_;
            this.tooltip = p_231708_;
            this.updateMessage();
            this.supportAdjusting = FloatOptions.supportAdjusting(this.instance);
            this.adjusting = false;
        }

        protected void updateMessage()
        {
            if (this.adjusting)
            {
                double d0 = ((Number)this.values.fromSliderValue(this.value)).doubleValue();
                Component component1 = FloatOptions.getTextComponent(this.instance, d0);

                if (component1 != null)
                {
                    this.setMessage(component1);
                }
            }
            else
            {
                if (this.instance instanceof SliderPercentageOptionOF)
                {
                    SliderPercentageOptionOF sliderpercentageoptionof = (SliderPercentageOptionOF)this.instance;
                    Component component = sliderpercentageoptionof.getOptionText();

                    if (component != null)
                    {
                        this.setMessage(component);
                    }
                }
                else
                {
                    this.setMessage(this.instance.toString.apply(this.instance.get()));
                }
            }
        }

        protected void applyValue()
        {
            if (!this.adjusting)
            {
                N n = this.instance.get();
                N n1 = this.values.fromSliderValue(this.value);

                if (!n1.equals(n))
                {
                    if (this.instance instanceof SliderPercentageOptionOF)
                    {
                        SliderPercentageOptionOF sliderpercentageoptionof = (SliderPercentageOptionOF)this.instance;
                        sliderpercentageoptionof.setOptionValue(((Number)n1).doubleValue());
                    }

                    this.instance.set(this.values.fromSliderValue(this.value));
                    this.options.save();
                }
            }
        }

        public List<FormattedCharSequence> getTooltip()
        {
            return this.tooltip.apply(this.values.fromSliderValue(this.value));
        }

        public void onClick(double mouseX, double mouseY)
        {
            if (this.supportAdjusting)
            {
                this.adjusting = true;
            }

            super.onClick(mouseX, mouseY);
        }

        protected void onDrag(double mouseX, double mouseY, double mouseDX, double mouseDY)
        {
            if (this.supportAdjusting)
            {
                this.adjusting = true;
            }

            super.onDrag(mouseX, mouseY, mouseDX, mouseDY);
        }

        public void onRelease(double mouseX, double mouseY)
        {
            if (this.adjusting)
            {
                this.adjusting = false;
                this.applyValue();
                this.updateMessage();
            }

            super.onRelease(mouseX, mouseY);
        }

        public OptionInstance getControlOption()
        {
            return this.instance;
        }
    }

    interface SliderableOrCyclableValueSet<T> extends OptionInstance.CycleableValueSet<T>, OptionInstance.SliderableValueSet<T>
    {
        boolean createCycleButton();

    default Function<OptionInstance<T>, AbstractWidget> createButton(OptionInstance.TooltipSupplier<T> p_231713_, Options p_231714_, int p_231715_, int p_231716_, int p_231717_)
        {
            return this.createCycleButton() ? OptionInstance.CycleableValueSet.super.createButton(p_231713_, p_231714_, p_231715_, p_231716_, p_231717_) : OptionInstance.SliderableValueSet.super.createButton(p_231713_, p_231714_, p_231715_, p_231716_, p_231717_);
        }
    }

    public interface SliderableValueSet<T> extends OptionInstance.ValueSet<T>
    {
        double toSliderValue(T p_231732_);

        T fromSliderValue(double p_231731_);

    default Function<OptionInstance<T>, AbstractWidget> createButton(OptionInstance.TooltipSupplier<T> p_231719_, Options p_231720_, int p_231721_, int p_231722_, int p_231723_)
        {
            return (p_231724_6_) ->
            {
                return new OptionInstance.OptionInstanceSliderButton<>(p_231720_, p_231721_, p_231722_, p_231723_, 20, p_231724_6_, this, p_231719_);
            };
        }
    }

    @FunctionalInterface
    public interface TooltipSupplier<T> extends Function<T, List<FormattedCharSequence>>
    {
    }

    public interface TooltipSupplierFactory<T> extends Function<Minecraft, OptionInstance.TooltipSupplier<T>>
    {
    }

    public static enum UnitDouble implements OptionInstance.SliderableValueSet<Double>
    {
        INSTANCE;

        public Optional<Double> validateValue(Double p_231747_)
        {
            return p_231747_ >= 0.0D && p_231747_ <= 1.0D ? Optional.of(p_231747_) : Optional.empty();
        }

        public double toSliderValue(Double p_231756_)
        {
            return p_231756_;
        }

        public Double fromSliderValue(double p_231741_)
        {
            return p_231741_;
        }

        public <R> OptionInstance.SliderableValueSet<R> xmap(final DoubleFunction <? extends R > p_231751_, final ToDoubleFunction <? super R > p_231752_)
        {
            return new OptionInstance.SliderableValueSet<R>()
            {
                public Optional<R> validateValue(R p_231773_)
                {
                    return UnitDouble.this.validateValue(p_231752_.applyAsDouble(p_231773_)).map(p_231751_::apply);
                }
                public double toSliderValue(R p_231777_)
                {
                    return UnitDouble.this.toSliderValue(p_231752_.applyAsDouble(p_231777_));
                }
                public R fromSliderValue(double p_231775_)
                {
                    return p_231751_.apply(UnitDouble.this.fromSliderValue(p_231775_));
                }
                public Codec<R> codec()
                {
                    return UnitDouble.this.codec().xmap(p_231751_::apply, p_231752_::applyAsDouble);
                }
            };
        }

        public Codec<Double> codec()
        {
            return Codec.either(Codec.doubleRange(0.0D, 1.0D), Codec.BOOL).xmap((p_231742_0_) ->
            {
                return p_231742_0_.map((p_231759_0_) -> {
                    return p_231759_0_;
                }, (p_231744_0_) -> {
                    return p_231744_0_ ? 1.0D : 0.0D;
                });
            }, Either::left);
        }
    }

    public interface ValueSet<T>
    {
        Function<OptionInstance<T>, AbstractWidget> createButton(OptionInstance.TooltipSupplier<T> p_231779_, Options p_231780_, int p_231781_, int p_231782_, int p_231783_);

        Optional<T> validateValue(T p_231784_);

        Codec<T> codec();
    }
}

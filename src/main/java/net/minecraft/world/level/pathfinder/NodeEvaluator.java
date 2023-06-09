package net.minecraft.world.level.pathfinder;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.PathNavigationRegion;

public abstract class NodeEvaluator
{
    protected PathNavigationRegion level;
    protected Mob mob;
    protected final Int2ObjectMap<Node> nodes = new Int2ObjectOpenHashMap<>();
    protected int entityWidth;
    protected int entityHeight;
    protected int entityDepth;
    protected boolean canPassDoors;
    protected boolean canOpenDoors;
    protected boolean canFloat;

    public void prepare(PathNavigationRegion p_77347_, Mob p_77348_)
    {
        this.level = p_77347_;
        this.mob = p_77348_;
        this.nodes.clear();
        this.entityWidth = Mth.floor(p_77348_.getBbWidth() + 1.0F);
        this.entityHeight = Mth.floor(p_77348_.getBbHeight() + 1.0F);
        this.entityDepth = Mth.floor(p_77348_.getBbWidth() + 1.0F);
    }

    public void done()
    {
        this.level = null;
        this.mob = null;
    }

    @Nullable
    protected Node getNode(BlockPos p_77350_)
    {
        return this.getNode(p_77350_.getX(), p_77350_.getY(), p_77350_.getZ());
    }

    @Nullable
    protected Node getNode(int pX, int pY, int pZ)
    {
        return this.nodes.computeIfAbsent(Node.createHash(pX, pY, pZ), (p_77332_) ->
        {
            return new Node(pX, pY, pZ);
        });
    }

    @Nullable
    public abstract Node getStart();

    @Nullable
    public abstract Target getGoal(double p_77322_, double p_77323_, double p_77324_);

    @Nullable
    protected Target getTargetFromNode(@Nullable Node p_230616_)
    {
        return p_230616_ != null ? new Target(p_230616_) : null;
    }

    public abstract int a(Node[] p_77353_, Node p_77354_);

    public abstract BlockPathTypes getBlockPathType(BlockGetter pBlockaccess, int pX, int pY, int pZ, Mob pEntityliving, int pXSize, int pYSize, int pZSize, boolean pCanBreakDoors, boolean pCanEnterDoors);

    public abstract BlockPathTypes getBlockPathType(BlockGetter pLevel, int pX, int pY, int pZ);

    public void setCanPassDoors(boolean pCanEnterDoors)
    {
        this.canPassDoors = pCanEnterDoors;
    }

    public void setCanOpenDoors(boolean pCanOpenDoors)
    {
        this.canOpenDoors = pCanOpenDoors;
    }

    public void setCanFloat(boolean pCanSwim)
    {
        this.canFloat = pCanSwim;
    }

    public boolean canPassDoors()
    {
        return this.canPassDoors;
    }

    public boolean canOpenDoors()
    {
        return this.canOpenDoors;
    }

    public boolean canFloat()
    {
        return this.canFloat;
    }
}

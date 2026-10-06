package net.favela.yaw.impl.modules.categories.movement;

import com.google.auto.service.AutoService;
import lombok.Getter;
import net.favela.yaw.impl.modules.Module;
import net.favela.yaw.impl.setting.settings.EnumSetting;
import net.favela.yaw.impl.setting.settings.NumberSetting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

import static net.favela.yaw.impl.util.wrapper.Wrapper.MC;

@AutoService(Module.class)
public class Step extends Module {

    public enum Mode {
        Vanilla, NCP
    }

    @Getter
    private static Step instance;

    public final EnumSetting<Mode> mode = enm("Mode", Mode.Vanilla);
    public final NumberSetting height = num("Height", 0.0f, 6.0f, 2.0f);

    private static final double DEFAULT_STEP = 0.6D;

    private double prevX;
    private double prevY;
    private double prevZ;
    private boolean hasPrev;

    public Step() {
        super("Step", "Step up blocks", Category.MOVEMENT);
        instance = this;
    }

    @Override
    public void onTick() {
        if (MC.player == null || MC.level == null) return;

        LocalPlayer player = MC.player;
        switch (mode.get()) {
            case Vanilla -> setStepHeight(player, DEFAULT_STEP);
            case NCP -> handleNCP(player);
        }

        prevX = player.getX();
        prevY = player.getY();
        prevZ = player.getZ();
        hasPrev = true;
    }

    @Override
    public void onDisable() {
        if (MC.player != null) {
            setStepHeight(MC.player, DEFAULT_STEP);
        }
        hasPrev = false;
    }

    public boolean isVanillaMode() {
        return isEnabled() && mode.get() == Mode.Vanilla;
    }

    public float getStepHeightValue() {
        return height.getFloat();
    }

    private void handleNCP(LocalPlayer player) {
        setStepHeight(player, DEFAULT_STEP);
        if (!hasPrev || !player.horizontalCollision) return;

        double climbed = player.getY() - prevY;
        float maxH = height.getFloat();
        if (climbed <= 0.5D || climbed > maxH) return;

        double[] offsets = getOffset(climbed);
        if (offsets == null) return;

        for (double offset : offsets) {
            player.connection.send(new ServerboundMovePlayerPacket.Pos(
                    prevX,
                    prevY + offset,
                    prevZ,
                    false,
                    player.horizontalCollision));
        }
        player.connection.send(new ServerboundMovePlayerPacket.Pos(
                player.getX(),
                player.getY(),
                player.getZ(),
                player.onGround(),
                player.horizontalCollision));
    }

    private static void setStepHeight(LocalPlayer player, double value) {
        AttributeInstance attr = player.getAttribute(Attributes.STEP_HEIGHT);
        if (attr != null && Math.abs(attr.getBaseValue() - value) > 1.0E-6D) {
            attr.setBaseValue(value);
        }
    }

    private double[] getOffset(double climbed) {
        int key = (int) Math.round(climbed * 10000.0D);
        double[] direct = offsetsForKey(key);
        if (direct != null) return direct;

        double[] keys = {0.75D, 0.8125D, 0.875D, 1.0D, 1.5D, 2.0D, 2.5D};
        double best = keys[0];
        double bestDiff = Math.abs(climbed - best);
        for (double k : keys) {
            double d = Math.abs(climbed - k);
            if (d < bestDiff) {
                bestDiff = d;
                best = k;
            }
        }
        return offsetsForKey((int) Math.round(best * 10000.0D));
    }

    private static double[] offsetsForKey(int key) {
        return switch (key) {
            case 7500, 10000 -> new double[]{0.42D, 0.753D};
            case 8125, 8750 -> new double[]{0.39D, 0.7D};
            case 15000 -> new double[]{0.42D, 0.75D, 1.0D, 1.16D, 1.23D, 1.2D};
            case 20000 -> new double[]{0.42D, 0.78D, 0.63D, 0.51D, 0.9D, 1.21D, 1.45D, 1.43D};
            case 25000 -> new double[]{0.425D, 0.821D, 0.699D, 0.599D, 1.022D, 1.372D, 1.652D, 1.869D, 2.019D, 1.907D};
            default -> null;
        };
    }
}

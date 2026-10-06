package net.favela.yaw.impl.modules.categories.player;

import com.google.auto.service.AutoService;
import com.mojang.authlib.GameProfile;
import net.favela.yaw.impl.modules.Module;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

import static net.favela.yaw.impl.util.wrapper.Wrapper.MC;

@AutoService(Module.class)
public class FakePlayer extends Module {

    private RemotePlayer fakePlayer;

    public FakePlayer() {
        super("FakePlayer", "Spawns fakeplayer for testing", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        if (MC.level == null || MC.player == null) {
            disable();
            return;
        }

        GameProfile profile = new GameProfile(UUID.randomUUID(), "FakePlayer");
        fakePlayer = new RemotePlayer((ClientLevel) MC.level, profile);

        Vec3 look = MC.player.getViewVector(1.0f);
        double spawnX = MC.player.getX() + look.x * 3.0;
        double spawnZ = MC.player.getZ() + look.z * 3.0;
        fakePlayer.snapTo(spawnX, MC.player.getY(), spawnZ, MC.player.getYRot(), MC.player.getXRot());

        int id = (int) profile.id().getLeastSignificantBits();
        fakePlayer.setId(id < 0 ? id : -id);
        fakePlayer.setHealth(20.0f);
        fakePlayer.setAbsorptionAmount(16.0f);

        fakePlayer.getInventory().clearContent();
        fakePlayer.getInventory().setItem(0, new ItemStack(Items.NETHERITE_SWORD));

        fakePlayer.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.NETHERITE_HELMET));
        fakePlayer.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.NETHERITE_CHESTPLATE));
        fakePlayer.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.NETHERITE_LEGGINGS));
        fakePlayer.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.NETHERITE_BOOTS));

        MC.level.addEntity(fakePlayer);
    }

    @Override
    public void onDisable() {
        if (MC.level != null && fakePlayer != null) {
            MC.level.removeEntity(fakePlayer.getId(), Entity.RemovalReason.DISCARDED);
        }
        fakePlayer = null;
    }
}

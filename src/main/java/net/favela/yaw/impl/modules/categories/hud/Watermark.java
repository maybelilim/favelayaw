package net.favela.yaw.impl.modules.categories.hud;

import com.google.auto.service.AutoService;
import net.favela.yaw.EntryPoint;
import net.favela.yaw.impl.gui.hud.Hud;
import net.favela.yaw.impl.modules.Module;
import net.favela.yaw.impl.modules.categories.client.GUI;
import net.favela.yaw.impl.modules.categories.client.HUD;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.awt.Color;

import static net.favela.yaw.impl.util.wrapper.Wrapper.MC;

@AutoService(Module.class)
public class Watermark extends Hud {

    public Watermark() {
        super("Watermark", "Client watermark", 40, 9);
    }

    @Override
    public void render(GuiGraphicsExtractor context) {
        super.render(context);

        Font font = MC.font;
        String name = EntryPoint.name();
        String version = " " + EntryPoint.version();

        int x = (int) getX();
        int y = (int) getY();

        HUD editor = HUD.getInstance();
        int nameColor = editor != null
                ? editor.getColor().getRGB()
                : (GUI.get() != null ? GUI.get().theme.getRGB() : Color.WHITE.getRGB());

        context.text(font, name, x, y, nameColor, true);
        context.text(font, version, x + font.width(name), y, 0xFFFFFFFF, true);

        setWidth(font.width(name + version));
        setHeight(font.lineHeight);
    }
}

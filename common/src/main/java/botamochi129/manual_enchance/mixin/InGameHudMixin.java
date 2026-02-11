package botamochi129.manual_enchance.mixin;

import botamochi129.manual_enchance.util.TrainAccessor;
import com.mojang.blaze3d.vertex.PoseStack;
import mtr.data.IGui;
import mtr.data.RailType;
import mtr.data.Route;
import mtr.data.Station;
import mtr.data.TrainClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class InGameHudMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.options.hideGui) return;

        TrainClient train = null;
        for (TrainClient tc : mtr.client.ClientData.TRAINS) {
            if (tc.isPlayerRiding(client.player) && tc.isHoldingKey(client.player)) {
                train = tc;
                break;
            }
        }

        if (train != null) {
            TrainAccessor accessor = (TrainAccessor) train;
            if (accessor.getIsCurrentlyManual()) {
                float speedKmh = train.getSpeed() * 20 * 3.6f;
                float maxSpeedKmh = 120f;
                int maxManualSpeedInt = train.maxManualSpeed;
                if (maxManualSpeedInt >= 0 && maxManualSpeedInt < RailType.values().length) {
                    maxSpeedKmh = RailType.values()[maxManualSpeedInt].speedLimit;
                }

                // GuiDrawingを初期化
                GuiDrawing drawing = new GuiDrawing(poseStack);

                drawCustomHUD(client, drawing, accessor.getManualNotch(), accessor.getReverser(),
                        accessor.manualEnchance$getDoorValue(), speedKmh, maxSpeedKmh, train, accessor);
            }
        }
    }

    @Unique
    private void drawCustomHUD(Minecraft client, GuiDrawing drawing, int notch, int reverser,
                               float doorValue, float speedKmh, float maxSpeedKmh, TrainClient train, TrainAccessor accessor) {
        int sw = client.getWindow().getGuiScaledWidth();
        int sh = client.getWindow().getGuiScaledHeight();

        int masconX = sw - 60;
        int masconY = sh / 2 - 40;
        int speedoX = sw / 2 - 100;
        int speedoY = sh - 75;

        // 背景
        drawing.drawRectangle(masconX - 5, masconY - 75, masconX + 20, masconY + 55, 0x88000000);

        // リバーサー
        String revLabel = (reverser == 1) ? "F" : (reverser == -1 ? "B" : "N");
        int revColor = (reverser == 1) ? 0xFFFFAA00 : (reverser == -1 ? 0xFFFF5555 : 0xFFFFFFFF);
        drawing.drawRectangle(masconX - 30, masconY - 10, masconX - 10, masconY + 10, 0x88000000);
        drawing.drawText(client.font, revLabel, masconX - 24, masconY - 4, revColor, true, IGui.MAX_LIGHT_GLOWING);
        drawing.drawText(client.font, "REV", masconX - 32, masconY - 22, 0xFFAAAAAA, true, IGui.MAX_LIGHT_GLOWING);

        // ノッチ
        drawing.drawRectangle(masconX - 2, masconY, masconX + 17, masconY + 1, 0xFFFFFFFF);
        int offset = notch * 8;
        int barColor = (notch > 0) ? 0xFFADFF2F : (notch == -9 ? 0xFFFF0000 : (notch < 0 ? 0xFF5555FF : 0xFFFFFFFF));
        drawing.drawRectangle(masconX - 4, masconY + offset - 2, masconX + 19, masconY + offset + 2, barColor);

        String label = (notch > 0) ? "P" + notch : (notch < 0 ? (notch == -9 ? "EB" : "B" + Math.abs(notch)) : "N");
        drawing.drawText(client.font, label, masconX + 25, masconY + offset - 4, 0xFFFFFFFF, true, IGui.MAX_LIGHT_GLOWING);

        // 戸閉灯
        boolean doorClosed = (doorValue == 0);
        drawing.drawRectangle(masconX - 36, masconY + 18, masconX - 6, masconY + 36, 0xFF111111);
        drawCircle(drawing, masconX - 21, masconY + 27, doorClosed ? 0xFFFFB300 : 0xFF4A3A1A);
        drawing.drawText(client.font, "DOOR", masconX - 33, masconY + 10, doorClosed ? 0xFFFFCC66 : 0xFF777777, true, IGui.MAX_LIGHT_GLOWING);

        drawAnalogSpeedometer(client, drawing, speedoX, speedoY, speedKmh, maxSpeedKmh);
        int railIndex = train.getIndex(accessor.manualEnchance$getRailProgress(), true);
        drawTIMS(client, drawing, (sw / 2) - 30, sh - 60, train, railIndex, accessor);
    }

    @Unique
    private void drawAnalogSpeedometer(Minecraft client, GuiDrawing drawing, int cx, int cy, float speed, float maxSpeed) {
        int r = 40;
        drawLargeDisk(drawing, cx, cy, r, 0xAA222222);
        drawLargeCircleOutline(drawing, cx, cy, r, 0xFFAAAAAA);

        int step = (maxSpeed > 160) ? 40 : 20;
        for (int s = 0; s <= (int) maxSpeed; s += step) {
            float angle = -225f + (s / maxSpeed) * 270f;
            float rad = (float) Math.toRadians(angle);
            int tx = cx + (int) (Math.cos(rad) * (r - 12));
            int ty = cy + (int) (Math.sin(rad) * (r - 12));
            drawing.drawText(client.font, String.valueOf(s), tx - client.font.width(String.valueOf(s)) / 2, ty - 4, 0xBBEEEEEE, false, IGui.MAX_LIGHT_GLOWING);
        }

        float sAngle = -225f + (Math.min(speed, maxSpeed) / maxSpeed) * 270f;
        float sRad = (float) Math.toRadians(sAngle);
        drawSimpleLine(drawing, cx, cy, cx + (int) (Math.cos(sRad) * (r - 5)), cy + (int) (Math.sin(sRad) * (r - 5)), 0xFFFF0000);

        String speedStr = String.format("%.0f", speed);
        drawing.drawText(client.font, speedStr, cx - client.font.width(speedStr) / 2, cy + 12, 0xFF00FF00, true, IGui.MAX_LIGHT_GLOWING);
        drawing.drawText(client.font, "km/h", cx - client.font.width("km/h") / 2, cy + 22, 0xFF00FF00, false, IGui.MAX_LIGHT_GLOWING);
    }

    @Unique
    private void drawCircle(GuiDrawing drawing, int cx, int cy, int color) {
        drawing.drawRectangle(cx - 2, cy - 3, cx + 3, cy - 2, color);
        drawing.drawRectangle(cx - 3, cy - 2, cx + 4, cy + 2, color);
        drawing.drawRectangle(cx - 2, cy + 2, cx + 3, cy + 3, color);
    }

    @Unique
    private void drawLargeDisk(GuiDrawing drawing, int cx, int cy, int r, int color) {
        for (int i = -r; i <= r; i++) {
            int w = (int) Math.sqrt(r * r - i * i);
            drawing.drawRectangle(cx - w, cy + i, cx + w, cy + i + 1, color);
        }
    }

    @Unique
    private void drawLargeCircleOutline(GuiDrawing drawing, int cx, int cy, int r, int color) {
        for (int a = 0; a < 360; a += 5) {
            double rad = Math.toRadians(a);
            int px = cx + (int) (Math.cos(rad) * r);
            int py = cy + (int) (Math.sin(rad) * r);
            drawing.drawRectangle(px, py, px + 1, py + 1, color);
        }
    }

    @Unique
    private void drawSimpleLine(GuiDrawing drawing, int x1, int y1, int x2, int y2, int color) {
        int dist = (int) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
        for (int i = 0; i <= dist; i++) {
            float t = (float) i / dist;
            drawing.drawRectangle((int) (x1 + (x2 - x1) * t), (int) (y1 + (y2 - y1) * t), (int) (x1 + (x2 - x1) * t) + 1, (int) (y1 + (y2 - y1) * t) + 1, color);
        }
    }

    @Unique
    private void drawTIMS(Minecraft client, GuiDrawing drawing, int x, int y, TrainClient train, int railIndex, TrainAccessor accessor) {
        int timsY = y - 40;
        int width = 165;
        int height = 75;

        drawing.drawRectangle(x, timsY, x + width, timsY + height, 0xAA000000);
        drawing.drawRectangle(x + 1, timsY + 1, x + width - 1, timsY + height - 1, 0xFF111111);

        String realTime = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
        long mcTicks = client.level.getDayTime() % 24000L;
        long mcHour = (mcTicks / 1000 + 6) % 24;
        long mcMin = (mcTicks % 1000) * 60 / 1000;
        String mcTime = String.format("%02d:%02d", mcHour, mcMin);

        float limitKmh = train.getRailSpeed(railIndex) * 20 * 3.6f;
        String limitStr = (limitKmh > 0) ? String.format("%.0f", limitKmh) : "---";

        Route thisRoute = train.getThisRoute();
        Station nextStation = train.getNextStation();

        drawing.drawText(client.font, "R:" + realTime + " M:" + mcTime, x + 5, timsY + 5, 0xFFBBBBBB, false, IGui.MAX_LIGHT_GLOWING);
        String routeName = (thisRoute == null) ? "Not In Service" : IGui.formatStationName(thisRoute.name);
        drawing.drawText(client.font, routeName, x + 5, timsY + 18, 0xFF00FFFF, false, IGui.MAX_LIGHT_GLOWING);

        if (nextStation != null) {
            String nextName = new Text("gui.manual_enchance.tims.next", IGui.formatStationName(nextStation.name)).getString();
            drawing.drawText(client.font, nextName, x + 5, timsY + 31, 0xFFFFFFFF, false, IGui.MAX_LIGHT_GLOWING);
        }

        int limitColor = (train.getSpeed() * 20 * 3.6f > limitKmh + 1) ? 0xFFFF5555 : 0xFFFFAA00;
        drawing.drawText(client.font, "LIMIT: " + limitStr + " km/h", x + 5, timsY + 46, limitColor, false, IGui.MAX_LIGHT_GLOWING);

        boolean isClosed = train.getDoorValue() == 0;
        String doorKey = isClosed ? "gui.manual_enchance.tims.door_closed" : "gui.manual_enchance.tims.door_open";
        drawing.drawText(client.font, new Text(doorKey).getString(), x + 5, timsY + 61, isClosed ? 0xFF00FF00 : 0xFFFF5555, false, IGui.MAX_LIGHT_GLOWING);

        String[] pantoNames = {"DOWN", "5.0m", "W51", "6.0m"};
        drawing.drawText(client.font, "PANTO: " + pantoNames[accessor.getPantographState()], x + 90, timsY + 46, 0xFFFFFFFF, false, IGui.MAX_LIGHT_GLOWING);
    }
}
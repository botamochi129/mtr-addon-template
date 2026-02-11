package botamochi129.manual_enchance.client;

import botamochi129.manual_enchance.util.TrainAccessor;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.event.events.client.ClientGuiEvent;
import mtr.data.IGui;
import mtr.data.RailType;
import mtr.data.Route;
import mtr.data.Station;
import mtr.data.TrainClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.resources.language.I18n;

public class ManualEnchanceClient {

    public static void init() {
        // Architecturyのイベントに登録。これによりForge/Fabric両方で描画されます。
        ClientGuiEvent.RENDER_HUD.register((poseStack, tickDelta) -> {
            renderManualHUD(poseStack, tickDelta);
        });
    }

    private static void renderManualHUD(PoseStack poseStack, float tickDelta) {
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

                drawCustomHUD(client, poseStack, accessor.getManualNotch(), accessor.getReverser(),
                        accessor.manualEnchance$getDoorValue(), speedKmh, maxSpeedKmh, train, accessor);
            }
        }
    }

    private static void drawCustomHUD(Minecraft client, PoseStack poseStack, int notch, int reverser,
                                      float doorValue, float speedKmh, float maxSpeedKmh, TrainClient train, TrainAccessor accessor) {
        int sw = client.getWindow().getGuiScaledWidth();
        int sh = client.getWindow().getGuiScaledHeight();

        int masconX = sw - 60;
        int masconY = sh / 2 - 40;
        int speedoX = sw / 2 - 100;
        int speedoY = sh - 75;

        // 背景
        GuiComponent.fill(poseStack, masconX - 5, masconY - 75, masconX + 20, masconY + 55, 0x88000000);

        // リバーサー
        String revLabel = (reverser == 1) ? "F" : (reverser == -1 ? "B" : "N");
        int revColor = (reverser == 1) ? 0xFFFFAA00 : (reverser == -1 ? 0xFFFF5555 : 0xFFFFFFFF);
        GuiComponent.fill(poseStack, masconX - 30, masconY - 10, masconX - 10, masconY + 10, 0x88000000);
        client.font.drawShadow(poseStack, revLabel, masconX - 24, masconY - 4, revColor);
        client.font.drawShadow(poseStack, "REV", masconX - 32, masconY - 22, 0xFFAAAAAA);

        // ノッチ
        GuiComponent.fill(poseStack, masconX - 2, masconY, masconX + 17, masconY + 1, 0xFFFFFFFF);
        int offset = notch * 8;
        int barColor = (notch > 0) ? 0xFFADFF2F : (notch == -9 ? 0xFFFF0000 : (notch < 0 ? 0xFF5555FF : 0xFFFFFFFF));
        GuiComponent.fill(poseStack, masconX - 4, masconY + offset - 2, masconX + 19, masconY + offset + 2, barColor);

        String label = (notch > 0) ? "P" + notch : (notch < 0 ? (notch == -9 ? "EB" : "B" + Math.abs(notch)) : "N");
        client.font.drawShadow(poseStack, label, masconX + 25, masconY + offset - 4, 0xFFFFFFFF);

        // 戸閉灯
        boolean doorClosed = (doorValue == 0);
        GuiComponent.fill(poseStack, masconX - 36, masconY + 18, masconX - 6, masconY + 36, 0xFF111111);
        drawCircle(poseStack, masconX - 21, masconY + 27, doorClosed ? 0xFFFFB300 : 0xFF4A3A1A);
        client.font.drawShadow(poseStack, "DOOR", masconX - 33, masconY + 10, doorClosed ? 0xFFFFCC66 : 0xFF777777);

        drawAnalogSpeedometer(client, poseStack, speedoX, speedoY, speedKmh, maxSpeedKmh);
        int railIndex = train.getIndex(accessor.manualEnchance$getRailProgress(), true);
        drawTIMS(client, poseStack, (sw / 2) - 30, sh - 60, train, railIndex, accessor);
    }

    private static void drawAnalogSpeedometer(Minecraft client, PoseStack poseStack, int cx, int cy, float speed, float maxSpeed) {
        int r = 40;
        drawLargeDisk(poseStack, cx, cy, r, 0xAA222222);
        drawLargeCircleOutline(poseStack, cx, cy, r, 0xFFAAAAAA);

        int step = (maxSpeed > 160) ? 40 : 20;
        for (int s = 0; s <= (int) maxSpeed; s += step) {
            float angle = -225f + (s / maxSpeed) * 270f;
            float rad = (float) Math.toRadians(angle);
            int tx = cx + (int) (Math.cos(rad) * (r - 12));
            int ty = cy + (int) (Math.sin(rad) * (r - 12));
            client.font.draw(poseStack, String.valueOf(s), tx - client.font.width(String.valueOf(s)) / 2f, ty - 4, 0xBBEEEEEE);
        }

        float sAngle = -225f + (Math.min(speed, maxSpeed) / maxSpeed) * 270f;
        float sRad = (float) Math.toRadians(sAngle);
        drawSimpleLine(poseStack, cx, cy, cx + (int) (Math.cos(sRad) * (r - 5)), cy + (int) (Math.sin(sRad) * (r - 5)), 0xFFFF0000);

        String speedStr = String.format("%.0f", speed);
        client.font.drawShadow(poseStack, speedStr, cx - client.font.width(speedStr) / 2f, cy + 12, 0xFF00FF00);
        client.font.draw(poseStack, "km/h", cx - client.font.width("km/h") / 2f, cy + 22, 0xFF00FF00);
    }

    private static void drawCircle(PoseStack poseStack, int cx, int cy, int color) {
        GuiComponent.fill(poseStack, cx - 2, cy - 3, cx + 3, cy - 2, color);
        GuiComponent.fill(poseStack, cx - 3, cy - 2, cx + 4, cy + 2, color);
        GuiComponent.fill(poseStack, cx - 2, cy + 2, cx + 3, cy + 3, color);
    }

    private static void drawLargeDisk(PoseStack poseStack, int cx, int cy, int r, int color) {
        for (int i = -r; i <= r; i++) {
            int w = (int) Math.sqrt(r * r - i * i);
            GuiComponent.fill(poseStack, cx - w, cy + i, cx + w, cy + i + 1, color);
        }
    }

    private static void drawLargeCircleOutline(PoseStack poseStack, int cx, int cy, int r, int color) {
        for (int a = 0; a < 360; a += 5) {
            double rad = Math.toRadians(a);
            int px = cx + (int) (Math.cos(rad) * r);
            int py = cy + (int) (Math.sin(rad) * r);
            GuiComponent.fill(poseStack, px, py, px + 1, py + 1, color);
        }
    }

    private static void drawSimpleLine(PoseStack poseStack, int x1, int y1, int x2, int y2, int color) {
        int dist = (int) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
        for (int i = 0; i <= dist; i++) {
            float t = (float) i / dist;
            GuiComponent.fill(poseStack, (int) (x1 + (x2 - x1) * t), (int) (y1 + (y2 - y1) * t), (int) (x1 + (x2 - x1) * t) + 1, (int) (y1 + (y2 - y1) * t) + 1, color);
        }
    }

    private static void drawTIMS(Minecraft client, PoseStack poseStack, int x, int y, TrainClient train, int railIndex, TrainAccessor accessor) {
        int timsY = y - 40;
        int width = 165;
        int height = 75;

        GuiComponent.fill(poseStack, x, timsY, x + width, timsY + height, 0xAA000000);
        GuiComponent.fill(poseStack, x + 1, timsY + 1, x + width - 1, timsY + height - 1, 0xFF111111);

        String realTime = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
        long mcTicks = client.level.getDayTime() % 24000L;
        long mcHour = (mcTicks / 1000 + 6) % 24;
        long mcMin = (mcTicks % 1000) * 60 / 1000;
        String mcTime = String.format("%02d:%02d", mcHour, mcMin);

        float limitKmh = train.getRailSpeed(railIndex) * 20 * 3.6f;
        String limitStr = (limitKmh > 0) ? String.format("%.0f", limitKmh) : "---";

        Route thisRoute = train.getThisRoute();
        Station nextStation = train.getNextStation();

        client.font.draw(poseStack, "R:" + realTime + " M:" + mcTime, x + 5, timsY + 5, 0xFFBBBBBB);
        String routeName = (thisRoute == null) ? "Not In Service" : IGui.formatStationName(thisRoute.name);
        client.font.draw(poseStack, routeName, x + 5, timsY + 18, 0xFF00FFFF);

        if (nextStation != null) {
            String nextName = I18n.get("gui.manual_enchance.tims.next", IGui.formatStationName(nextStation.name));
            client.font.draw(poseStack, nextName, x + 5, timsY + 31, 0xFFFFFFFF);
        }

        int limitColor = (train.getSpeed() * 20 * 3.6f > limitKmh + 1) ? 0xFFFF5555 : 0xFFFFAA00;
        client.font.draw(poseStack, "LIMIT: " + limitStr + " km/h", x + 5, timsY + 46, limitColor);

        boolean isClosed = train.getDoorValue() == 0;
        String doorKey = isClosed ? "gui.manual_enchance.tims.door_closed" : "gui.manual_enchance.tims.door_open";
        client.font.draw(poseStack, I18n.get(doorKey), x + 5, timsY + 61, isClosed ? 0xFF00FF00 : 0xFFFF5555);

        String[] pantoNames = {"DOWN", "5.0m", "W51", "6.0m"};
        client.font.draw(poseStack, "PANTO: " + pantoNames[accessor.getPantographState()], x + 90, timsY + 46, 0xFFFFFFFF);
    }
}
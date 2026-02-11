package botamochi129.manual_enchance.mixin;

import botamochi129.manual_enchance.client.IPartInfo;
import botamochi129.manual_enchance.client.PantoHelper;
import botamochi129.manual_enchance.util.TrainAccessor;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import mtr.client.DynamicTrainModel;
import mtr.client.IDrawing;
import mtr.client.ScrollingText;
import mtr.data.Route;
import mtr.data.Station;
import mtr.data.TrainClient;
import mtr.mappings.UtilitiesClient;
import mtr.render.MoreRenderLayers;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Mixin(value = DynamicTrainModel.class, remap = false)
public abstract class DynamicTrainModelMixin {

    @Inject(method = "shouldSkipRender", at = @At("HEAD"), cancellable = true)
    private void onShouldSkipRender(JsonObject partObject, CallbackInfoReturnable<Boolean> cir) {
        if (!partObject.has("render_condition")) return;
        String condition = partObject.get("render_condition").getAsString();

        TrainClient train = PantoHelper.getCurrentTrain();
        if (train == null) return;

        int pantoState = ((TrainAccessor) train).getPantographState();
        switch (condition) {
            case "PANTAGRAPH_DOWN":
                if (pantoState != 0) cir.setReturnValue(true);
                break;
            case "PANTAGRAPH_5M":
                if (pantoState != 1) cir.setReturnValue(true);
                break;
            case "PANTAGRAPH_W51":
                if (pantoState != 2) cir.setReturnValue(true);
                break;
            case "PANTAGRAPH_6M":
                if (pantoState != 3) cir.setReturnValue(true);
                break;
        }
    }

    @Shadow
    @Final
    private Map<String, Set<?>> partsInfo; // アクセス修飾子を private に合わせる

    @Shadow
    protected abstract boolean shouldSkipRender(JsonObject partObject);

    @Shadow
    protected abstract float getOffsetX(JsonObject partObject);

    @Shadow
    protected abstract float getOffsetZ(JsonObject partObject);

    @Shadow
    protected abstract void iterateParts(int currentCar, int trainCars, java.util.function.Consumer<JsonObject> callback);

    @Inject(method = "renderTextDisplays", at = @At("TAIL"))
    private void onRenderRollsigns(PoseStack matrices, MultiBufferSource vertexConsumers, Font font, MultiBufferSource.BufferSource immediate, Route thisRoute, Route nextRoute, Station thisStation, Station nextStation, Station lastStation, String customDestination, int car, int totalCars, boolean atPlatform, List<ScrollingText> scrollingTexts, CallbackInfo ci) {

        TrainClient train = PantoHelper.getCurrentTrain();
        if (!(train instanceof TrainAccessor accessor)) return;

        this.iterateParts(car, totalCars, (partObject) -> {
            if (!partObject.has("name")) return;
            String name = partObject.get("name").getAsString();

            if (partObject.has("rollsign") && partObject.get("rollsign").getAsBoolean()) {
                String rollsignId = partObject.has("rollsign_id") ? partObject.get("rollsign_id").getAsString() : name;
                int totalSteps = partObject.has("rollsign_steps") ? partObject.get("rollsign_steps").getAsInt() : 1;

                accessor.setRollsignSteps(rollsignId, totalSteps);

                if (!accessor.getRollsignIndices().containsKey(rollsignId)) {
                    accessor.setRollsignIndex(rollsignId, 0);
                }

                if (this.shouldSkipRender(partObject)) return;
                if (!this.partsInfo.containsKey(name)) return;

                String texturePath = partObject.has("rollsign_texture") ? partObject.get("rollsign_texture").getAsString() : "mtr:example/example.png";
                boolean mirror = partObject.has("mirror") && partObject.get("mirror").getAsBoolean();
                boolean enableAnimation = !partObject.has("rollsign_animation") || partObject.get("rollsign_animation").getAsBoolean();

                float vStep = 1.0f / totalSteps;
                float displayOffset = enableAnimation ? accessor.getRollsignOffset(rollsignId) : (float) accessor.getRollsignIndex(rollsignId);

                float v1 = displayOffset * vStep;
                float v2 = v1 + vStep;

                // FabricのIdentifierではなくMinecraftのResourceLocationを使用
                ResourceLocation textureId = new ResourceLocation(texturePath);

                float xOffset = this.getOffsetX(partObject);
                float zOffset = this.getOffsetZ(partObject);

                partObject.getAsJsonArray("positions").forEach((positionElement) -> {
                    float posX = positionElement.getAsJsonArray().get(0).getAsFloat() + xOffset;
                    float posZ = positionElement.getAsJsonArray().get(1).getAsFloat() + zOffset;

                    Set<?> partInfoSet = this.partsInfo.get(name);
                    if (partInfoSet != null) {
                        partInfoSet.forEach((partInfoObj) -> {
                            IPartInfo partInfo = (IPartInfo) partInfoObj;

                            matrices.pushPose(); // matrices.push() -> pushPose()
                            matrices.translate(posX / 16.0, 0.0, posZ / 16.0);
                            if (mirror) UtilitiesClient.rotateYDegrees(matrices, 180.0F);

                            matrices.translate(-partInfo.getOriginX(), -partInfo.getOriginY(), partInfo.getOriginZ());
                            UtilitiesClient.rotateZDegrees(matrices, partInfo.getRotationZ());
                            UtilitiesClient.rotateYDegrees(matrices, partInfo.getRotationY());
                            UtilitiesClient.rotateXDegrees(matrices, partInfo.getRotationX());
                            matrices.translate(-partInfo.getOffsetX(), -partInfo.getOffsetY(), partInfo.getOffsetZ() - 0.001);

                            IDrawing.drawTexture(
                                    matrices,
                                    vertexConsumers.getBuffer(MoreRenderLayers.getLight(textureId, false)),
                                    -partInfo.getWidth() / 2, -partInfo.getHeight() / 2,
                                    partInfo.getWidth(), partInfo.getHeight(),
                                    0.0f, v1, 1.0f, v2,
                                    Direction.UP, -1, 15728880
                            );
                            matrices.popPose(); // matrices.pop() -> popPose()
                        });
                    }
                });
            }
        });
    }
}
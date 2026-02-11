package botamochi129.manual_enchance.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import mtr.render.RenderDrivingOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RenderDrivingOverlay.class, remap = false)
public class RenderDrivingOverlayMixin {

    /**
     * MTR標準の運転HUD描画をキャンセルし、自作HUDのみを表示させるようにします。
     * 1.19.2 Mojang Mapping では MatrixStack ではなく PoseStack を使用します。
     */
    @Inject(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void cancelMTRHUD(PoseStack poseStack, CallbackInfo ci) {
        ci.cancel();
    }
}
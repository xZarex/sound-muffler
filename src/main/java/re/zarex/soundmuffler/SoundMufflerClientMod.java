package re.zarex.soundmuffler;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.math.BlockPos;
import re.zarex.soundmuffler.block.SoundMuffler;

import java.util.ArrayList;

public class SoundMufflerClientMod implements ClientModInitializer {
	private static final ArrayList<BlockPos> soundMufflers = new ArrayList<>();

	public static void clearMufflers()
	{
		soundMufflers.clear();
	}

	public static void addMuffler(BlockPos pos)
	{
		soundMufflers.add(new BlockPos(pos));
	}

	public static void removeMuffler(BlockPos pos)
	{
		soundMufflers.remove(new BlockPos(pos));
	}

	public static boolean isMufflerClose(double x, double y, double z)
	{
		for (BlockPos mufflerPos : soundMufflers) {
			{
				double d = mufflerPos.getX() - x;
				double e = mufflerPos.getY() - y;
				double f = mufflerPos.getZ() - z;
				if (d * d + e * e + f * f < 40)
					return true;
			}
		}

		return false;
	}
	@Override
	public void onInitializeClient() {
		SoundMuffler.RegisterClient();

		ClientPlayNetworking.registerGlobalReceiver(SoundMufflerMod.PACKET_CLEAR, (client, handler, buf, responseSender) -> {
			client.execute(() -> {
				SoundMufflerClientMod.clearMufflers();
			});
		});

		ClientPlayNetworking.registerGlobalReceiver(SoundMufflerMod.PACKET_ADD, (client, handler, buf, responseSender) -> {
			BlockPos pos = buf.readBlockPos();
			client.execute(() -> {
				SoundMufflerClientMod.addMuffler(pos);
			});
		});

		ClientPlayNetworking.registerGlobalReceiver(SoundMufflerMod.PACKET_REMOVE, (client, handler, buf, responseSender) -> {
			BlockPos pos = buf.readBlockPos();
			client.execute(() -> {
				SoundMufflerClientMod.removeMuffler(pos);
			});
		});
	}
}

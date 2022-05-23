package re.zarex.soundmuffler;

import net.fabricmc.api.ModInitializer;;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import re.zarex.soundmuffler.block.SoundMuffler;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

public class SoundMufflerMod implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("soundmuffler");
	private static final HashMap<String, ArrayList<ArrayList<Integer>>> soundMufflers = new HashMap<>();
	private static boolean loadedHashMap = false;

	public static final Identifier PACKET_CLEAR = new Identifier("soundmuffler", "clearsoundmufflers");
	public static final Identifier PACKET_ADD = new Identifier("soundmuffler", "addsoundmuffler");
	public static final Identifier PACKET_REMOVE = new Identifier("soundmuffler", "removesoundmuffler");

	public static void saveHashmap(MinecraftServer server, String filename, HashMap<String, ArrayList<ArrayList<Integer>>> hashMap)
	{
		Path path = server.getSavePath(WorldSavePath.ROOT);
		Path configPath = path.resolve( filename ).toAbsolutePath();
		try {
			Files.deleteIfExists(configPath);
			Files.createFile(configPath);
			ObjectOutputStream oos = null;
			FileOutputStream fout = null;
			try {
				File file = new File(configPath.toString());
				fout = new FileOutputStream(file);
				oos = new ObjectOutputStream(fout);

				oos.writeObject(hashMap);
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				if(oos != null){
					oos.close();
				}
			}

		} catch (Exception e) {
			LOGGER.info("Could not save data file! "+e.toString());
		}
	}

	public static void loadHashmap(MinecraftServer server, String filename, HashMap<String, ArrayList<ArrayList<Integer>>> hashMap)
	{
		Path path = server.getSavePath(WorldSavePath.ROOT);
		Path configPath = path.resolve( filename ).toAbsolutePath();
		try {
			ObjectInputStream objectinputstream = null;
			try {
				File file = new File(configPath.toString());
				FileInputStream streamIn = new FileInputStream(file);
				objectinputstream = new ObjectInputStream(streamIn);
				hashMap.putAll ((HashMap<String, ArrayList<ArrayList<Integer>>>)objectinputstream.readObject());
			} catch (Exception e) {
				LOGGER.debug("Could not load data file, seems like there aren't any mufflers placed");
			} finally {
				if(objectinputstream != null){
					objectinputstream .close();
				}
			}
		} catch (Exception ex2) {
			LOGGER.debug("Could not load data file, seems like there aren't any mufflers placed");
		}

		loadedHashMap = true;
	}


	public static void addMuffler(ServerWorld world, BlockPos pos)
	{
		if (soundMufflers.containsKey(world.getRegistryKey().toString()) == false)
			soundMufflers.put(world.getRegistryKey().toString(), new ArrayList<>());
		for (ArrayList<Integer> mufflerPos : soundMufflers.get(world.getRegistryKey().toString())) {
			if (mufflerPos.get(0) == pos.getX() && mufflerPos.get(1) == pos.getY() && mufflerPos.get(2) == pos.getZ())
				return;
		}



		ArrayList<Integer> list = new ArrayList<>();
		list.add(pos.getX());
		list.add(pos.getY());
		list.add(pos.getZ());
		soundMufflers.get(world.getRegistryKey().toString()).add(list);

		world.getPlayers().forEach(serverPlayerEntity -> {
			PacketByteBuf buf = PacketByteBufs.create();
			buf.writeBlockPos(pos);
			ServerPlayNetworking.send(serverPlayerEntity, SoundMufflerMod.PACKET_ADD, buf);
		});

		saveHashmap(world.getServer(), "soundmufflers.data", soundMufflers);
	}

	public static void removeMuffler(ServerWorld world, BlockPos pos)
	{
		ArrayList<Integer> entry = null;
		for (ArrayList<Integer> mufflerPos : soundMufflers.get(world.getRegistryKey().toString())) {
			if (mufflerPos.get(0) == pos.getX() && mufflerPos.get(1) == pos.getY() && mufflerPos.get(2) == pos.getZ()) {
				entry = mufflerPos;
				break;
			}
		}
		if (entry != null)
			soundMufflers.get(world.getRegistryKey().toString()).remove(entry);

		world.getPlayers().forEach(serverPlayerEntity -> {
			PacketByteBuf buf = PacketByteBufs.create();
			buf.writeBlockPos(pos);
			ServerPlayNetworking.send(serverPlayerEntity, SoundMufflerMod.PACKET_REMOVE, buf);
		});

		saveHashmap(world.getServer(), "soundmufflers.data", soundMufflers);
	}

	public static void sendStartPackets(ServerPlayerEntity player, ServerWorld world)
	{
		if (loadedHashMap == false)
			loadHashmap(world.getServer(), "soundmufflers.data", soundMufflers);

		{
			PacketByteBuf buf = PacketByteBufs.create();
			ServerPlayNetworking.send(player, SoundMufflerMod.PACKET_CLEAR, buf);
		}

		if (soundMufflers.containsKey(world.getRegistryKey().toString()))
		{
			for (ArrayList<Integer> muffler : soundMufflers.get(world.getRegistryKey().toString())) {
				PacketByteBuf buf = PacketByteBufs.create();
				buf.writeBlockPos(new BlockPos(muffler.get(0), muffler.get(1), muffler.get(2)));
				ServerPlayNetworking.send(player, SoundMufflerMod.PACKET_ADD, buf);
			}
		}
	}
	@Override
	public void onInitialize() {
		SoundMuffler.Register();

		ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((ServerPlayerEntity player, ServerWorld origin, ServerWorld destination) -> {
			sendStartPackets(player, destination);
		});

	}
}

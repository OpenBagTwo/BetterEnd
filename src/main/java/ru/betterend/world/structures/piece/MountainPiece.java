package ru.betterend.world.structures.piece;

import java.util.Map;
import java.util.Random;

import com.google.common.collect.Maps;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.Biome;
import ru.betterend.noise.OpenSimplexNoise;
import ru.betterend.registry.EndBiomes;
import ru.betterend.util.MHelper;

public abstract class MountainPiece extends BasePiece {
	protected Map<Integer, Integer> heightmap = Maps.newHashMap();
	protected OpenSimplexNoise noise1;
	protected OpenSimplexNoise noise2;
	protected BlockPos center;
	protected float radius;
	protected float height;
	protected float r2;
	protected Identifier biomeID;
	protected int seed1;
	protected int seed2;
	
	public MountainPiece(StructurePieceType type, BlockPos center, float radius, float height, Random random, Biome biome) {
		super(type, random.nextInt());
		this.center = center;
		this.radius = radius;
		this.height = height;
		this.r2 = radius * radius;
		this.seed1 = random.nextInt();
		this.seed2 = random.nextInt();
		this.noise1 = new OpenSimplexNoise(this.seed1);
		this.noise2 = new OpenSimplexNoise(this.seed2);
		this.biomeID = EndBiomes.getBiomeID(biome);
		makeBoundingBox();
	}

	public MountainPiece(StructurePieceType type, StructureManager manager, CompoundTag tag) {
		super(type, tag);
		makeBoundingBox();
	}

	@Override
	protected void toNbt(CompoundTag tag) {
		tag.put("center", NbtHelper.fromBlockPos(center));
		tag.putFloat("radius", radius);
		tag.putFloat("height", height);
		tag.putString("biome", biomeID.toString());
		tag.putInt("seed1", seed1);
		tag.putInt("seed2", seed2);
	}

	@Override
	protected void fromNbt(CompoundTag tag) {
		center = NbtHelper.toBlockPos(tag.getCompound("center"));
		radius = tag.getFloat("radius");
		height = tag.getFloat("height");
		biomeID = new Identifier(tag.getString("biome"));
		r2 = radius * radius;
		seed1 = tag.getInt("seed1");
		seed2 = tag.getInt("seed2");
		noise1 = new OpenSimplexNoise(seed1);
		noise2 = new OpenSimplexNoise(seed2);
	}
	
	private int getHeight(StructureWorldAccess world, BlockPos pos) {
		int p = ((pos.getX() & 2047) << 11) | (pos.getZ() & 2047);
		int h = heightmap.getOrDefault(p, Integer.MIN_VALUE);
		if (h > Integer.MIN_VALUE) {
			return h;
		}
		
		if (!EndBiomes.getBiomeID(world.getBiome(pos)).equals(biomeID)) {
			heightmap.put(p, -10);
			return -10;
		}
		h = world.getTopY(Type.WORLD_SURFACE_WG, pos.getX(), pos.getZ());
		h = MathHelper.abs(h - center.getY());
		if (h > 4) {
			h = 4 - h;
			heightmap.put(p, h);
			return h;
		}
		
		h = MHelper.floor(noise2.eval(pos.getX() * 0.01, pos.getZ() * 0.01) * noise2.eval(pos.getX() * 0.002, pos.getZ() * 0.002) * 8 + 8);
		
		if (h < 0) {
			heightmap.put(p, 0);
			return 0;
		}
		
		heightmap.put(p, h);
		
		return h;
	}
	
	protected float getHeightClamp(StructureWorldAccess world, int radius, int posX, int posZ) {
		Mutable mut = new Mutable();
		float height = 0;
		float max = 0;
		for (int x = -radius; x <= radius; x++) {
			mut.setX(posX + x);
			int x2 = x * x;
			for (int z = -radius; z <= radius; z++) {
				mut.setZ(posZ + z);
				int z2 = z * z;
				float mult = 1 - (float) Math.sqrt(x2 + z2) / radius;
				if (mult > 0) {
					max += mult;
					height += getHeight(world, mut) * mult;
				}
			}
		}
		height /= max;
		return MathHelper.clamp(height / radius, 0, 1);
	}
	
	private void makeBoundingBox() {
		int minX = MHelper.floor(center.getX() - radius);
		int minZ = MHelper.floor(center.getZ() - radius);
		int maxX = MHelper.floor(center.getX() + radius + 1);
		int maxZ = MHelper.floor(center.getZ() + radius + 1);
		this.boundingBox = new BlockBox(minX, minZ, maxX, maxZ);
	}
}

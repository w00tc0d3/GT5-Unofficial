package gregtech.common.tileentities.boilers;

import gregtech.api.enums.Dyes;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.enums.Textures;
import gregtech.api.enums.Textures.BlockIcons;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.objects.GT_RenderedTexture;
import gregtech.api.util.GT_ModHandler;
import gregtech.api.util.GT_OreDictUnificator;
import gregtech.common.gui.GT_Container_Boiler;
import gregtech.common.gui.GT_GUIContainer_Boiler;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;

public class GT_MetaTileEntity_Boiler_Steel
  extends GT_MetaTileEntity_Boiler
{
  public GT_MetaTileEntity_Boiler_Steel(int aID, String aName, String aNameRegional)
  {
    super(aID, aName, aNameRegional, "Faster than the Bronze Boiler", new ITexture[0]);
  }
  
  public GT_MetaTileEntity_Boiler_Steel(String aName, int aTier, String aDescription, ITexture[][][] aTextures)
  {
    super(aName, aTier, aDescription, aTextures);
  }
  
  public ITexture[][][] getTextureSet(ITexture[] aTextures)
  {
    ITexture[][][] rTextures = new ITexture[5][17][];
    for (byte i = -1; i < 16; i = (byte)(i + 1))
    {ITexture[] tmp0 ={ new GT_RenderedTexture(Textures.BlockIcons.MACHINE_STEELBRICKS_BOTTOM, Dyes.getModulation(i, Dyes._NULL.mRGBa)) };
      rTextures[0][(i + 1)] = tmp0;
      ITexture[] tmp1 ={ new GT_RenderedTexture(Textures.BlockIcons.MACHINE_STEELBRICKS_TOP, Dyes.getModulation(i, Dyes._NULL.mRGBa)), new GT_RenderedTexture(Textures.BlockIcons.OVERLAY_PIPE) };
rTextures[1][(i + 1)] = tmp1;
      ITexture[] tmp2 ={ new GT_RenderedTexture(Textures.BlockIcons.MACHINE_STEELBRICKS_SIDE, Dyes.getModulation(i, Dyes._NULL.mRGBa)), new GT_RenderedTexture(Textures.BlockIcons.OVERLAY_PIPE) };
rTextures[2][(i + 1)] = tmp2;
      ITexture[] tmp4 ={ new GT_RenderedTexture(Textures.BlockIcons.MACHINE_STEELBRICKS_SIDE, Dyes.getModulation(i, Dyes._NULL.mRGBa)), new GT_RenderedTexture(Textures.BlockIcons.BOILER_FRONT) };
rTextures[3][(i + 1)] = tmp4;
     ITexture[] tmp5 ={ new GT_RenderedTexture(Textures.BlockIcons.MACHINE_STEELBRICKS_SIDE, Dyes.getModulation(i, Dyes._NULL.mRGBa)), new GT_RenderedTexture(Textures.BlockIcons.BOILER_FRONT_ACTIVE) };
rTextures[4][(i + 1)] = tmp5;
    }
    return rTextures;
  }
  
  public int maxProgresstime()
  {
    return 1000;
  }
  
  public Object getServerGUI(int aID, InventoryPlayer aPlayerInventory, IGregTechTileEntity aBaseMetaTileEntity)
  {
    return new GT_Container_Boiler(aPlayerInventory, aBaseMetaTileEntity, 32000);
  }
  
  public Object getClientGUI(int aID, InventoryPlayer aPlayerInventory, IGregTechTileEntity aBaseMetaTileEntity)
  {
    return new GT_GUIContainer_Boiler(aPlayerInventory, aBaseMetaTileEntity, "SteelBoiler.png", 32000);
  }
  
  public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity)
  {
    return new GT_MetaTileEntity_Boiler_Steel(this.mName, this.mTier, this.mDescription, this.mTextures);
  }
  
  public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick)
  {
    if ((aBaseMetaTileEntity.isServerSide()) && (aTick > 20L))
    {
      if (this.mTemperature <= 20)
      {
        this.mTemperature = 20;
        this.mLossTimer = 0;
      }
      if (++this.mLossTimer > 40)
      {
        this.mTemperature -= 1;
        this.mLossTimer = 0;
      }
      for (byte i = 1; (this.mSteam != null) && (i < 6); i = (byte)(i + 1)) {
        if (i != aBaseMetaTileEntity.getFrontFacing())
        {
          IFluidHandler tTileEntity = aBaseMetaTileEntity.getITankContainerAtSide(i);
          if (tTileEntity != null)
          {
            FluidStack tDrained = aBaseMetaTileEntity.drain(ForgeDirection.getOrientation(i), Math.max(1, this.mSteam.amount / 2), false);
            if (tDrained != null)
            {
              int tFilledAmount = tTileEntity.fill(ForgeDirection.getOrientation(i).getOpposite(), tDrained, false);
              if (tFilledAmount > 0) {
                tTileEntity.fill(ForgeDirection.getOrientation(i).getOpposite(), aBaseMetaTileEntity.drain(ForgeDirection.getOrientation(i), tFilledAmount, true), true);
              }
            }
          }
        }
      }
      if (aTick % 10L == 0L) {
        if (this.mTemperature > 100)
        {
          if ((this.mFluid == null) || (!GT_ModHandler.isWater(this.mFluid)) || (this.mFluid.amount <= 0))
          {
            this.mHadNoWater = true;
          }
          else
          {
            if (this.mHadNoWater)
            {
              aBaseMetaTileEntity.doExplosion(2048L);
              return;
            }
            this.mFluid.amount -= 1;
            if (this.mSteam == null) {
              this.mSteam = GT_ModHandler.getSteam(150L);
            } else if (GT_ModHandler.isSteam(this.mSteam)) {
              this.mSteam.amount += 150;
            } else {
              this.mSteam = GT_ModHandler.getSteam(150L);
            }
          }
        }
        else {
          this.mHadNoWater = false;
        }
      }
      if ((this.mSteam != null) && 
        (this.mSteam.amount > 32000))
      {
        sendSound((byte)1);
        this.mSteam.amount = 24000;
      }
      if ((this.mProcessingEnergy <= 0) && (aBaseMetaTileEntity.isAllowedToWork()) && 
        (this.mInventory[2] != null)) {
        if ((GT_OreDictUnificator.isItemStackInstanceOf(this.mInventory[2], OrePrefixes.gem.get(Materials.Coal))) || (GT_OreDictUnificator.isItemStackInstanceOf(this.mInventory[2], OrePrefixes.dust.get(Materials.Coal))) || (GT_OreDictUnificator.isItemStackInstanceOf(this.mInventory[2], OrePrefixes.dustImpure.get(Materials.Coal))) || (GT_OreDictUnificator.isItemStackInstanceOf(this.mInventory[2], OrePrefixes.crushed.get(Materials.Coal))))
        {
          this.mProcessingEnergy += 160;
          aBaseMetaTileEntity.decrStackSize(2, 1);
          if (aBaseMetaTileEntity.getRandomNumber(3) == 0) {
            aBaseMetaTileEntity.addStackToSlot(3, GT_OreDictUnificator.get(OrePrefixes.dustTiny, Materials.DarkAsh, 1L));
          }
        }
        else if ((GT_OreDictUnificator.isItemStackInstanceOf(this.mInventory[2], OrePrefixes.gem.get(Materials.Charcoal))) || (GT_OreDictUnificator.isItemStackInstanceOf(this.mInventory[2], OrePrefixes.dust.get(Materials.Charcoal))) || (GT_OreDictUnificator.isItemStackInstanceOf(this.mInventory[2], OrePrefixes.dustImpure.get(Materials.Charcoal))) || (GT_OreDictUnificator.isItemStackInstanceOf(this.mInventory[2], OrePrefixes.crushed.get(Materials.Charcoal))))
        {
          this.mProcessingEnergy += 160;
          aBaseMetaTileEntity.decrStackSize(2, 1);
          if (aBaseMetaTileEntity.getRandomNumber(3) == 0) {
            aBaseMetaTileEntity.addStackToSlot(3, GT_OreDictUnificator.get(OrePrefixes.dustTiny, Materials.Ash, 1L));
          }
        }
        else if (GT_OreDictUnificator.isItemStackInstanceOf(this.mInventory[2], "fuelCoke"))
        {
          this.mProcessingEnergy += 640;
          aBaseMetaTileEntity.decrStackSize(2, 1);
          if (aBaseMetaTileEntity.getRandomNumber(2) == 0) {
            aBaseMetaTileEntity.addStackToSlot(3, GT_OreDictUnificator.get(OrePrefixes.dustTiny, Materials.Ash, 1L));
          }
        }
        else if ((GT_OreDictUnificator.isItemStackInstanceOf(this.mInventory[2], OrePrefixes.gem.get(Materials.Lignite))) || (GT_OreDictUnificator.isItemStackInstanceOf(this.mInventory[2], OrePrefixes.dust.get(Materials.Lignite))) || (GT_OreDictUnificator.isItemStackInstanceOf(this.mInventory[2], OrePrefixes.dustImpure.get(Materials.Lignite))) || (GT_OreDictUnificator.isItemStackInstanceOf(this.mInventory[2], OrePrefixes.crushed.get(Materials.Lignite))))
        {
          this.mProcessingEnergy += 40;
          aBaseMetaTileEntity.decrStackSize(2, 1);
          if (aBaseMetaTileEntity.getRandomNumber(8) == 0) {
            aBaseMetaTileEntity.addStackToSlot(3, GT_OreDictUnificator.get(OrePrefixes.dustTiny, Materials.DarkAsh, 1L));
          }
        }
      }
      if ((this.mTemperature < 1000) && (this.mProcessingEnergy > 0) && (aTick % 12L == 0L))
      {
        this.mProcessingEnergy -= 2;
        this.mTemperature += 1;
      }
      aBaseMetaTileEntity.setActive(this.mProcessingEnergy > 0);
    }
  }
}

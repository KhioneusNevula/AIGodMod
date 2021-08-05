A mod utilizing AI to do stuff in Minecraft. This project is mostly an exploration of AI; although it is hopefully going to be a playable mod as well with survival utility and some kind of progression in the future. (Not sure what my plans would be for that, though)

- current plan: Create a House-GAN that makes Minecraft houses using a Generative Adversarial Network
- Future possible plans:
- - Perhaps make an RL algorithm of some kind that assists the player
- - Model GAN to generate custom creatures

## Issues
- Java python integration is not fully implemented
- GAN model, after training on 75 structures, still produces pretty indefinite results

## Resources
- https://paperswithcode.com/dataset/minecraft-house
- https://www.computer.org/csdl/proceedings-article/iccv/2019/480300b764/1hVleI3lT4A


Mapping Names:
=============================
By default, the MDK is configured to use the official mapping names from Mojang for methods and fields 
in the Minecraft codebase. These names are covered by a specific license. All modders should be aware of this
license, if you do not agree with it you can change your mapping names to other crowdsourced names in your 
build.gradle. For the latest license text, refer to the mapping file itself, or the reference copy here:
https://github.com/MinecraftForge/MCPConfig/blob/master/Mojang.md

Additional Resources: 
=========================
Community Documentation: http://mcforge.readthedocs.io/en/latest/gettingstarted/  
LexManos' Install Video: https://www.youtube.com/watch?v=8VEdtQLuLO0  
Forge Forum: https://forums.minecraftforge.net/  
Forge Discord: https://discord.gg/UvedJ9m  

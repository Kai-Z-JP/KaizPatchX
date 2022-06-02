# KaizPatch

[English](README.en.md) | [日本語](README.md) | [Русский](README.ru.md) | [한국어](README.ko.md)

The translation was done using DeepL.\
If there are any incorrect translations, I would appreciate it if you could point them out.

DeepLを使用して翻訳しました。\
正しくない翻訳などあれば、指摘していただけると幸いです。

[![MCVer](https://img.shields.io/badge/Minecraft-1.7.10-brightgreen)](https://www.minecraft.net/)
[![ForgeVer](https://img.shields.io/badge/Forge-10.13.4.1614-important)](https://files.minecraftforge.net/maven/net/minecraftforge/forge/index_1.7.10.html)
[![DLCount](https://img.shields.io/github/downloads/Kai-Z-JP/KaizPatchX/total)](https://github.com/Kai-Z-JP/KaizPatchX/releases)
[![DLCountLatest](https://img.shields.io/github/downloads/Kai-Z-JP/KaizPatchX/latest/total)](https://github.com/Kai-Z-JP/KaizPatchX/releases/latest)

## For RTM 1.12.2 Users

Please use [fixRTM](https://www.curseforge.com/minecraft/mc-mods/fixrtm).

## What is this?

KaizPatch is an unofficial modification of NGTLib/RTM/MCTE.

## Notes

**I do NOT take any responsibility for the use of this mod.**

The slope rail will be removed during installation. Please be aware of this. Also, please backup your world by
appropriate means before installation.

## Base

- NGTLib: 1.7.10.32
- RTM: 1.7.10.41
- MCTE: 1.7.10.16

## How to install

0.Backup all your data.\
1.Delete NGTLib/RTM/MCTE from your mods folder.\
2.Put KaizPatch in your mods folder.\
3.If you want to load an existing world, you will be prompted with a warning, so make sure you understand it well before
pressing OK.\
4.Done!

## Ported Features

The following features have been ported from versions 1.10.2/1.12.2.

- EZ wrench curve
- Wrench(1.10.2)
- Wrench(1.12.2)
- Rail cant
- Rail replacement on click
- CustomButton
- ActionParts
- DataMap editing in the model selection GUI
- Camera
- Ticket Vendor
- Speaker(Some specification changes)

## Additional Features

- Multi-threading of model pack loading.
- Passenger perspective follows the movement of the train.
- Extension of rail and wire rendering distance
- Notch/Brake step customization
- Acceleration/Deceleration Custom
  ```
  Maximum speed at each notch step(The number of notches is also determined here.)
  "maxSpeed": [ 1 step, 2 step, infinite...(If notDisplayCab is false, it will be fixed to 5 steps) ],

  Acceleration
  "accelerateions": [ 1 step, 2 step, infinite...(If notDisplayCab is false, it will be fixed to 5 steps) maxSpeedの個数と同値],

  Deceleration(The number of brake steps is also determined here.)
  "deccelerations": [inertia step, -1 step, -2 step, infinite...(If notDisplayCab is false, it will be fixed to -8 steps)],
  ```
- By wheel-clicking on a Crossing/Signal/Point/Light/Turnstile/Train Stop/Train model ,you can itemize the entire model
  and DataMap.
- Rotate Crossing/Signal/Turnstile/Light/Sign Model in 1 degree increments by placing them while holding down the Shift
  key.
- Tab completion for commands
- Switch wrench behavior(`/rtm use1122marker (true|false)`)

## Thanks

I'd like to take this opportunity to thank [NGT-5479](https://twitter.com/ngt5479) for giving us permission to
distribute it, and everyone who helped us debug the hell out of it over the course of about six months.

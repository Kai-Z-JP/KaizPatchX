# KaizPatch

[English](README.md) | [日本語](README.ja.md) | [Русский](README.ru.md) | [한국어](README.ko.md)

[![MCVer](https://img.shields.io/badge/Minecraft-1.7.10-brightgreen)](https://www.minecraft.net/)
[![ForgeVer](https://img.shields.io/badge/Forge-10.13.4.1614-important)](https://files.minecraftforge.net/maven/net/minecraftforge/forge/index_1.7.10.html)
[![DLCount](https://img.shields.io/github/downloads/Kai-Z-JP/KaizPatchX/total)](https://github.com/Kai-Z-JP/KaizPatchX/releases)
[![DLCountLatest](https://img.shields.io/github/downloads/Kai-Z-JP/KaizPatchX/latest/total)](https://github.com/Kai-Z-JP/KaizPatchX/releases/latest)

## Minecraft1.12.2のユーザー向け

[fixRTM](https://github.com/fixrtm/fixrtm) をご利用ください。

## これはなに

KaizPatchはNGTLib・RTM・MCTEの非公式改造物です。\
何故かPatchという名前になっていますが、本来の意味での"パッチ"ではありません。

## 注意事項

**当制作物の使用による一切の責任を負いません。**

導入時に坂レールが削除されます。ご注意ください。 また、導入前に適切な手段でワールドのバックアップをお願いします。

## Base

- NGTLib: 1.7.10.32
- RTM: 1.7.10.41
- MCTE: 1.7.10.16

## 導入方法

0.全データのバックアップを取ってください\
1.NGTLib・RTM・MCTEを抜いてください\
2.KaizPatchを入れてください\
3.既存のワールドを読み込む場合は英語でなんとかかんとかOK?と聞かれますのでよく理解したうえでOKを押してください。\
4.完了！

## 移植機能

1.10.2及び1.12.2より以下の機能を移植しています。

- レンチ吸いつき
- レンチ(1.10.2)
- レンチ(1.12.2)
- カント機能
- レールクリックでの置換機能
- CustomButton
- ActionParts
- モデル選択画面でのDataMap編集
- カメラ
- 券売機
- スピーカー(一部仕様変更)

## 追加機能
- モデルパックロードのマルチスレッド化
- 乗客の視点追従
- レール・ワイヤー描画距離の延長
- ノッチ/ブレーキ段数カスタム
- 加速度/減速度カスタム
  ```
  ノッチ段数での最高速度(ノッチ段数をここで決定)
  "maxSpeed": [ 1段, 2段, 以下無限(notDisplayCabがfalseの場合は5段固定) ],
  加速度
  "accelerateions": [ 1段, 2段, 以下無限(notDisplayCabがfalseの場合は5段固定) maxSpeedの個数と同値],
  減速度(ブレーキ段数をここで決定)
  "deccelerations": [惰性, -1段, -2段, 以下無限(notDisplayCabがfalseの場合は-8段固定)],
  ```
- 踏切・信号・転轍機・照明・改札・車止め・車両モデルをモデルとDataMapごとスポイト
- 踏切・信号・転轍機・照明・標識モデルをShiftキー押しながら設置することで1度刻みで回転できる
- コマンドのタブ補完
- レンチの挙動を切り替え(`/rtm use1122marker (true|false)`)

## 謝辞

公開にあたりまして、配布許可を頂いた[NGT-5479](https://twitter.com/ngt5479) 様、約半年間に渡って地獄の様なデバッグに協力していただいた皆様に、この場をお借りして感謝申し上げます。

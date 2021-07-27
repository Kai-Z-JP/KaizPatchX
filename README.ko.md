# KaizPatch

[English](README.md) | [日本語](README.ja.md) | [Русский](README.ru.md) | [한국어](README.ko.md)

[![MCVer](https://img.shields.io/badge/Minecraft-1.7.10-brightgreen)](https://www.minecraft.net/)
[![ForgeVer](https://img.shields.io/badge/Forge-10.13.4.1614-important)](https://files.minecraftforge.net/maven/net/minecraftforge/forge/index_1.7.10.html)
[![DLCount](https://img.shields.io/github/downloads/Kai-Z-JP/KaizPatchX/total)](https://github.com/Kai-Z-JP/KaizPatchX/releases)
[![DLCountLatest](https://img.shields.io/github/downloads/Kai-Z-JP/KaizPatchX/latest/total)](https://github.com/Kai-Z-JP/KaizPatchX/releases/latest)

## 이것은
KaizPatch는 NGTLib・RTM・MCTE의 비공식 개조판입니다.

## 주의사항

**이 제작물의 사용에 대하여 일절 책임지지 않습니다.**

도입 시에 언덕 레일(노란색 마커)가 삭제됩니다. 주의해 주세요. 또한, 도입 전에 적절한 방법으로 월드의 백업을 부탁드립니다.

## Base

- NGTLib: 1.7.10.32
- RTM: 1.7.10.41
- MCTE: 1.7.10.16

## 도입 방법

0. 모든 데이터의 백업을 해 두세요.
1. NGTLib, RTM, MCTE을 삭제해 주세요.
2. KaizPatch를 넣어 주세요.
3. 기존 월드를 불러올 경우에는 영어로 뭔가 OK?라고 물어볼텐데, 잘 이해한 다음에 OK를 눌러주세요.
4. 끝！

## 이식된 기능

1.10.2 및 1.12.2로부터 이하의 기능을 이식했습니다.

- 쉬운 곡선 렌치
- 렌치(1.10.2)
- 렌치(1.12.2)
- 캔트 기능
- 클릭으로 레일 종류 변경
- CustomButton
- ActionParts
- 모델 선택 화면에서의 DataMap편집
- 카메라
- 자동발매기
- 스피커(일부 사양 변경)

## 추가 기능
- 모델 팩 로딩의 멀티스레드화
- 객실 시점이 열차의 움직임을 따라감
- 레일, 와이어의 렌더링 거리 증가
- 놋치, 브레이크 단 수 커스텀
- 가속도, 감속도 커스텀
  ```
  놋치 단수의 최고속도(놋치 단 수를 여기서 결정)
  "maxSpeed": [ 1단, 2단, 이하 무한(notDisplayCab가 false인 경우는 5단 고정) ],
  가속도
  "accelerateions": [ 1단, 2단, 이하 무한(notDisplayCab가 false인 경우는 5단 고정) maxSpeed의 값과 같은 사양],
  감속도(브레이크 단 수를 여기서 결정)
  "deccelerations": [타성(0), -1단, -2단, 이하 무한(notDisplayCab가 false인 경우는 -8단 고정)],
  ```
- 건널목 차단기, 신호기, 선로전환기, 조명, 개찰기, 차막이, 차량 모델을 모델, DataMap별 스포이트
- 건널목 차단기, 신호기, 선로전환기, 조명, 표지 모델을 Shift키를 누르면서 설치하면 회전시킬 수 있음
- 명령어 태그 보완
- 렌치의 작동 방식을 바꿀 수 있음(`/rtm use1122marker (true|false)`)

## 감사

공개에 앞서 배포 허가를 내어주신 [NGT-5479](https://twitter.com/ngt5479)님, 약 반년동안 지옥같은 디버깅에 협력해주신 여러분께, 이 자리를 빌려 감사드립니다.

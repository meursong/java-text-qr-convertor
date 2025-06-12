# Text to QR Code Converter

격리된 내부망 환경에서 텍스트 데이터를 QR 코드로 변환하여 외부망으로 안전하게 전송하는 고정밀 변환 도구입니다.

## 주요 기능

- **대용량 텍스트 처리**: 자동 분할 및 시퀀스 QR 코드 생성
- **데이터 무결성**: SHA-256/MD5 체크섬을 통한 검증
- **압축 지원**: GZIP 압축으로 효율적인 데이터 전송
- **다양한 입력 방식**: 파일, 클립보드, 직접 입력
- **고신뢰성**: ZXing 라이브러리의 Error Correction Level H 사용

## 시스템 요구사항

- Java 11 이상
- Maven 3.6 이상

## 빌드 방법

```bash
mvn clean package
```

## 실행 방법

### GUI 모드
```bash
java -jar target/text-qr-converter-1.0.0.jar
```

### CLI 모드
```bash
java -jar target/text-qr-converter-1.0.0.jar --cli <input-file> <output-directory> [--compress]
```

## 사용 예시

### GUI에서 사용
1. 텍스트 입력 영역에 변환할 텍스트 입력
2. "Enable Compression" 옵션 선택 (선택사항)
3. "Generate QR Codes" 버튼 클릭
4. 생성된 QR 코드 확인 및 저장

### CLI에서 사용
```bash
# 로그 파일을 QR 코드로 변환
java -jar text-qr-converter.jar --cli application.log output_qr/ --compress

# 생성된 파일들:
# output_qr/qr_12345678_001.png
# output_qr/qr_12345678_002.png
# output_qr/qr_composite_12345678.png
# output_qr/session_12345678.txt
```

## 기술 스택

- **언어**: Java 11
- **QR 코드 생성**: ZXing 3.5.2
- **UI 프레임워크**: JavaFX 17.0.2
- **로깅**: SLF4J + Logback
- **빌드 도구**: Maven

## 프로젝트 구조

```
src/main/java/com/textqr/converter/
├── core/
│   ├── QRGenerator.java          # QR 코드 생성 엔진
│   └── TextInputHandler.java     # 텍스트 입력 처리
├── model/
│   ├── QRChunk.java             # QR 청크 데이터 모델
│   └── QRSession.java           # QR 세션 관리
├── ui/
│   └── MainWindow.java          # JavaFX UI
├── util/
│   ├── ChecksumUtil.java        # 체크섬 계산
│   ├── CompressionUtil.java     # 압축 처리
│   └── TextChunker.java         # 텍스트 분할
└── TextQRConverterApp.java      # 메인 애플리케이션
```

## 라이선스

이 프로젝트는 MIT 라이선스를 따릅니다.
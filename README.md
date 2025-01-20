# 🏷️ 프로젝트 소개
### 목적
두핸즈 구성원들이 직장에서의 성취를 기반으로 일의 즐거움을 느끼고 동기부여를 받을 수 있도록 사내 성과를 가시화하여 보여줄 수 있는 앱 서비스 개발 공모전

### 주요기능
- 어드민 : 어드민 계정으로 로그인하여 어드민 페이지 접속(계정 생성 ,계정관리,게시글 관리, 게시글 작성)
- 로그인 : 어드민에서 부여한 ID/PW로 로그인
- 구성원 정보 : 본인 정보(사번, 소속, 성함, 캐릭터, 입사일, 레벨)확인 , 비밀번호 변경기능, 캐릭터 변경기능
- 경험치 현황 : 누적 경험치 그래프 및 올해 경험치 그래프 조회
- 팀 퀘스트 현황 : 팀 퀘스트 달성 현황을 주/월별로 확인
- 경험치 목록 : 경험치 달성 목록 확인
- 게시판 : 어드민에서 등록한 게시글 조회
- 알림 : 모바일 홈 또는 잠금화면에서 팝업으로 알림메시지가 보여지도록 구현(경험치 획득 시 알림, 게시글 게재 시 알림, 주/월별 퀘스트 달성여부 알림)

<div id="b">
 
# ⚙️ 개발 환경
| **Category**               | **Tech Stack**                                                                                                                                                                                              |
|----------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Server & DevOps**        | <img src="https://img.shields.io/badge/GCP-4285F4?style=for-the-badge&logo=googlecloud&logoColor=white"> <img src="https://img.shields.io/badge/docker-2496ED?style=for-the-badge&logo=docker&logoColor=white">    |
| **Backend**                | <img src="https://img.shields.io/badge/java-007396?style=for-the-badge&logo=OpenJDK&logoColor=white"> <img src="https://img.shields.io/badge/springboot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white">   |
| **Frontend**               | <img src="https://img.shields.io/badge/Next.js-000000?style=for-the-badge&logo=nextdotjs&logoColor=white"> <img src="https://img.shields.io/badge/Typescript-3178C6?style=for-the-badge&logo=Typescript&logoColor=white"> <img src="https://img.shields.io/badge/Vercel-000000?style=for-the-badge&logo=Vercel&logoColor=white"> <img src="https://img.shields.io/badge/ReactQuery-FF4154?style=for-the-badge&logo=ReactQuery&logoColor=white"> <img src="https://img.shields.io/badge/TailwindCss-06B6D4?style=for-the-badge&logo=TailwindCss&logoColor=white"> <img src="https://img.shields.io/badge/jotai-000000?style=for-the-badge&logo=ghostery&logoColor=white"> |
| **Test Code & Performance**| <img src="https://img.shields.io/badge/JUnit5-25A162?style=for-the-badge&logo=JUnit5&logoColor=white"> <img src="https://img.shields.io/badge/Mockito-47A248?style=for-the-badge&logo=Mockito&logoColor=white">  |
| **DB**                     | <img src="https://img.shields.io/badge/mongodb-47A248?style=for-the-badge&logo=mongodb&logoColor=white">                      |
| **OS**                     | <img src="https://img.shields.io/badge/Ubuntu-E95420?style=for-the-badge&logo=Ubuntu&logoColor=white">                                                                                                        |
| **Communication**          | <img src="https://img.shields.io/badge/slack-4A154B?style=for-the-badge&logo=slack&logoColor=white"> <img src="https://img.shields.io/badge/notion-000000?style=for-the-badge&logo=notion&logoColor=white"> <img src="https://img.shields.io/badge/Discord-5865F2?style=for-the-badge&logo=Discord&logoColor=white"> |
<div id="c">

 # [📆 개발 기간 25.01.05 ~25.01.15]

 
<div id="d">
 
# 🖇️ 팀 구성
| 류승범(Back-End)                          | 김원기(Back-End)                         | 김진우(Front-End)                         |
|-------------------------------|------------------------------|-------------------------------|
| [![류승범](https://github.com/W-llama.png)](https://github.com/W-llama) | [![김원기](https://github.com/WonGi-Kim.png)](https://github.com/WonGi-Kim) | [![김진우](https://github.com/Jinviz.png)](https://github.com/Jinviz) |

<div id="e">
  
# [📋서비스 아키텍처]

![playus](https://github.com/user-attachments/assets/82c45e6d-9791-49f1-8204-f82031c05e79)

<div id="f">

# 📑 ERD DIAGRAM

<div id="g">

![KakaoTalk_20250119_200148913](https://github.com/user-attachments/assets/98dd01de-f937-438d-a286-29e31b441caa)

# 🗂️ API 명세서 [http://localhost:8080/swagger](http://localhost:8080/swagger-ui/index.html#/)

![스크린샷 2025-01-20 104611](https://github.com/user-attachments/assets/83ebf859-0e06-427a-90fe-49b6b2c2e415)
![스크린샷 2025-01-20 104621](https://github.com/user-attachments/assets/da1a5cce-4f8c-4704-a770-5ff70085d9f6)
![스크린샷 2025-01-20 104627](https://github.com/user-attachments/assets/f53e8a61-3601-4027-8d5b-33e6ac1d25a6)
![스크린샷 2025-01-20 104633](https://github.com/user-attachments/assets/43e53698-e423-40a5-820d-10be0565d5db)

<div id="h">

# 💻 필수 API KEY
Google Sheets 접근을 위한 API 키 파일`(google.json)`이 필요합니다.

파일 위치: `resources/googleSheet/`

<div id="i">

# 🔑 환경 변수
```
MYSQL_ENDPOINT= {your_mysql_url}
MYSQL_PORT = {your_Mysql_port}
MYSQL_DBNAME= {your_database_name}
MYSQL_USERNAME= {your_database_username}
MYSQL_PASSWORD= {your_database_password}
JWT_SECRET_KEY= {your_key}
JWT_ACCESS_EXPIRE_TIME= 3600000
JWT_REFRESH_EXPIRE_TIME= 1209600000
MONGODB_URI= {your_mongodb_uri}
GOOGLE_SHEET_ID={your_google_sheet_id}
```
---





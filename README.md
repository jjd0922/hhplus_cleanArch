## ERD

![erd](https://github.com/user-attachments/assets/609bd084-7b04-41e9-8793-7819f2a40980)

## 설명
- 유저 - 수강신청 - 강의
-    1 : N  ,  N : 1 구조
- 데이터의 변경 이력을 추적하기 위해 각 테이블에 created_at ,modified_at 추가
- 수강신청 순서 보장을 위해 수강신청 테이블에 ir_sequence 컬럼 추가
- 데이터의 정합성을 위해 수강신청 상태(ir_state)를 추가

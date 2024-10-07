## download historical statistics

1. https://financeapi.net/ 가서 계정 생성하면 api key 발급된다.
2. 아래 명령으로 json 다운로드

    ```
    curl -s -X 'GET' \
    'https://yfapi.net/v8/finance/chart/FNGU?range=10y&region=US&interval=1d&lang=en&events=div%2Csplit' \
    -H 'accept: application/json' \
    -H 'X-API-KEY: taCRJzEwki2PQWTWc2Xo75MUmtbGkW662kDxmUrW' > FNGU.json
    ```

3. resources 폴더에 json 파일 이동

## 리포트

https://examples.javacodegeeks.com/java-development/desktop-java/jfreechart/jfree-candlestick-chart-example/

캔들 스틱 차트를 보여 주면서 해보자.
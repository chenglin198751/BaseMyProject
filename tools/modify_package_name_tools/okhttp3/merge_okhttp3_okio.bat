
echo off

cls

rd /s /Q okhttp3

echo "------------- Delete okhttp3 dir success"

md okhttp3

echo "------------- Create okhttp3 dir success"
pause

copy .\okio-1.14.0.jar .\okhttp3\okio-1.14.0.jar

copy .\okhttp-3.10.0.jar .\okhttp3\okhttp-3.10.0.jar

cd okhttp3

echo "------------- Copy okhttp3 jar package success"
pause

jar -xvf .\okio-1.14.0.jar

echo "------------- Unzip okio jar package success"
pause

jar -xvf .\okhttp-3.10.0.jar

echo "------------- Unzip okhttp-3 jar package success"
pause

del /f *.jar

jar -cvfM merge_okhttp3_okio.jar .\

echo "------------- Merge okhttp-3 jar package success"
pause

java -jar ..\jarjar-1.4.jar process ..\rule_okio.txt .\merge_okhttp3_okio.jar .\modify_okio.jar

echo "------------- Modify okio package name success"
pause

java -jar ..\jarjar-1.4.jar process ..\rule_okhttp.txt .\modify_okio.jar .\360_sdk_okhttp3.jar

echo "------------- Modify okhttp-3 package name success"
pause

copy .\360_sdk_okhttp3.jar ..\360_sdk_okhttp3.jar

cd ..

rd /s /Q okhttp3

echo "------------- Create 360 okhttp-3 jar package success"
pause

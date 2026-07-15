@rem Licensed to the Apache Software Foundation (ASF) under one
@rem or more contributor license agreements.  See the NOTICE file
@rem distributed with this work for additional information
@rem regarding copyright ownership.  The ASF licenses this file
@rem to you under the Apache License, Version 2.0 (the
@rem "License"); you may not use this file except in compliance
@rem with the License.  You may obtain a copy of the License at
@rem
@rem    https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing,
@rem software distributed under the License is distributed on an
@rem "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@rem KIND, either express or implied.  See the License for the
@rem specific language governing permissions and limitations
@rem under the License.
@rem ----------------------------------------------------------------------------

@rem ----------------------------------------------------------------------------
@rem Maven Start Up Batch script
@rem
@rem Required ENV vars:
@rem JAVA_HOME - location of a JDK home dir
@rem
@rem Optional ENV vars
@rem M2_HOME - location of maven2's installed home dir
@rem MAVEN_BATCH_ECHO - set to 'on' to enable the echoing of the batch commands
@rem MAVEN_BATCH_PAUSE - set to 'on' to wait for a keystroke before ending
@rem MAVEN_OPTS - parameters passed to the Java VM when running Maven
@rem     e.g. to debug Maven itself, use
@rem set MAVEN_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000
@rem MAVEN_SKIP_RC - flag to disable loading of mavenrc files
@rem ----------------------------------------------------------------------------

@if "%MAVEN_SKIP_RC%"=="" @echo off
if exist "%USERPROFILE%\mavenrc_pre.bat" call "%USERPROFILE%\mavenrc_pre.bat"
if exist "%USERPROFILE%\mavenrc_post.bat" call "%USERPROFILE%\mavenrc_post.bat"
if exist "%HOME%\mavenrc_pre.bat" call "%HOME%\mavenrc_pre.bat"
if exist "%HOME%\mavenrc_post.bat" call "%HOME%\mavenrc_post.bat"

@setlocal

set MAVEN_PROJECTBASEDIR=%CD%
set WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar

if "%JAVA_HOME%"=="" (
    echo Java 17 is required. Set JAVA_HOME to a valid JDK installation.
    exit /b 1
)
for /f "tokens=3" %%v in ('"%JAVA_HOME%\bin\java.exe" -version 2^>^&1 ^| findstr /I "version"') do set JAVA_VERSION=%%~v
echo %JAVA_VERSION% | findstr /B /C:"17." >nul
if errorlevel 1 (
    echo Java 17 is required. Current JAVA_HOME is %JAVA_HOME%
    exit /b 1
)

if not exist "%WRAPPER_JAR%" (
    echo - Downloading maven-wrapper.jar
    powershell -Command "Invoke-WebRequest -Uri 'https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar' -OutFile '%WRAPPER_JAR%'"
)

"%JAVA_HOME%\bin\java.exe" %MAVEN_OPTS% -classpath "%WRAPPER_JAR%" "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" org.apache.maven.wrapper.MavenWrapperMain %*
@endlocal

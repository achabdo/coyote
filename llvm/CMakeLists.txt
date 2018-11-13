cmake_minimum_required(VERSION 2.8)

include(ExternalProject)

if(OS STREQUAL "linux")
  set(LINUX YES)
elseif(OS STREQUAL "macosx")
  set(MACOSX YES)
elseif(OS STREQUAL "windows")
  set(WINDOWS YES)
endif()

if (WINDOWS)
  # set(CMAKE_CROSSCOMPILING TRUE) 
  set(CMAKE_SYSTEM_NAME Windows)

  #should be passed from build.sh, just here to prevent warning
  set(CMAKE_RC_COMPILER "/usr/bin/x86_64-w64-mingw32-windres")
  
  #these are not actually used by mira but find_package does not always resolve them, use JAVA_HOME env var to prevent paths?
  set(JAVA_AWT_LIBRARY "/usr/lib/jvm/java-7-openjdk-amd64/jre/lib/amd64/libawt.so")
  set(JAVA_JVM_LIBRARY "/usr/lib/jvm/java-7-openjdk-amd64/jre/lib/amd64/server/libjvm.so")

  # adjust the default behaviour of the FIND_XXX() commands:
  # search headers and libraries in the target environment, search
  # programs in the host environment
  set(CMAKE_FIND_ROOT_PATH_MODE_PROGRAM NEVER)
  set(CMAKE_FIND_ROOT_PATH_MODE_LIBRARY ONLY)
  set(CMAKE_FIND_ROOT_PATH_MODE_INCLUDE ONLY)
endif()

project(mira-llvm)

find_package(JNI REQUIRED)

message (STATUS "JNI_INCLUDE_DIRS=${JNI_INCLUDE_DIRS}")
message (STATUS "JNI_LIBRARIES=${JNI_LIBRARIES}")

if (ARCH STREQUAL "x86")
  set(X86 YES)
elseif (ARCH STREQUAL "x86_64")
  set(X86_64 YES)
endif()

set(C_CXX_FLAGS "${C_CXX_FLAGS} -fvisibility=hidden -fdata-sections -ffunction-sections")
if(MACOSX AND X86_64)
  set(C_CXX_FLAGS "${C_CXX_FLAGS} -arch x86_64 -mmacosx-version-min=10.7")
  set(LD_FLAGS "${LD_FLAGS} -arch x86_64 -mmacosx-version-min=10.7")
elseif(LINUX AND X86)
  set(C_CXX_FLAGS "${C_CXX_FLAGS} -m32")
  set(LD_FLAGS "${LD_FLAGS} -m64")
elseif(LINUX AND X86_64)
  set(C_CXX_FLAGS "${C_CXX_FLAGS} -m64")
  set(LD_FLAGS "${LD_FLAGS} -m64")
elseif(WINDOWS AND X86)
  set(C_CXX_FLAGS "${C_CXX_FLAGS} -m32")
  set(LD_FLAGS "${LD_FLAGS} -m32")
elseif(WINDOWS AND X86_64)
  set(C_CXX_FLAGS "${C_CXX_FLAGS} -m64")
  set(LD_FLAGS "${LD_FLAGS} -m64")
endif()

if(MACOSX)
  exec_program(xcode-select ARGS --print-path OUTPUT_VARIABLE XCODE_PATH)
  set(SYSROOT "${XCODE_PATH}/Platforms/MacOSX.platform/Developer/SDKs/MacOSX10.9.sdk")
  if(NOT IS_DIRECTORY ${SYSROOT})
    set(SYSROOT "${XCODE_PATH}/Platforms/MacOSX.platform/Developer/SDKs/MacOSX10.8.sdk")
  endif()
  if(NOT IS_DIRECTORY ${SYSROOT})
    message(FATAL_ERROR "No supported MacOSX SDK found")
  endif()
  set(C_CXX_FLAGS "${C_CXX_FLAGS} -isysroot ${SYSROOT}")
endif()

set(LLVM_ARCHS "X86")
set(LLVM_C_FLAGS "${C_CXX_FLAGS} -fvisibility=hidden -fdata-sections -ffunction-sections")
set(LLVM_CXX_FLAGS "${C_CXX_FLAGS} -fvisibility=hidden -fdata-sections -ffunction-sections")

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} ${C_CXX_FLAGS}")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${C_CXX_FLAGS} -fno-rtti -std=c++0x")
set(CMAKE_INSTALL_DO_STRIP YES)
if(MACOSX)
  set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -stdlib=libc++")
endif()

set(LLVM_INCLUDES "src/main/swig/include")

add_definitions(-D__STDC_LIMIT_MACROS -D__STDC_CONSTANT_MACROS)
include_directories(${JAVA_INCLUDE_PATH} ${JAVA_INCLUDE_PATH2} ${LLVM_INCLUDES})

# This was generated using 'llvm-config --libs core bitreader bitwriter irreader ipo arm x86 aarch64 debuginfo linker option instrumentation objcarcopts'
set(LLVM_LIBS
   "LLVM-5.0"
)

# This was generated using 'llvm-config --ldflags'
if(LINUX)
  set(LLVM_LDFLAGS
    -L"/usr/local/lib"
    -lrt -ldl -lpthread -lz
  )
elseif(WINDOWS)
  set(LLVM_LDFLAGS
    -L"${CMAKE_BINARY_DIR}/llvm/lib"
    -ldbghelp -static-libgcc -static-libstdc++
  )
elseif(MACOSX)
  set(LLVM_LDFLAGS
    -L"${CMAKE_BINARY_DIR}/llvm/lib"
    -lcurses -lpthread -lz
  )
endif()

set(SRC
  src/main/native/main.c
  src/main/native/LLVM_wrap.c
  src/main/native/LLVMExtra.cpp
)

add_executable(mira-llvm ${SRC})
target_link_libraries(mira-llvm ${LLVM_LIBS} ${LLVM_LDFLAGS})
set_target_properties(mira-llvm PROPERTIES LINKER_LANGUAGE CXX)
if(LINUX)
  set_target_properties(mira-llvm PROPERTIES LINK_FLAGS "-Wl,--gc-sections -Wl,--version-script=${CMAKE_SOURCE_DIR}/src/main/native/exports_linux.sym")
elseif(MACOSX)
  set_target_properties(mira-llvm PROPERTIES LINK_FLAGS "-Wl,-dead_strip -Wl,-exported_symbols_list -Wl,${CMAKE_SOURCE_DIR}/src/main/native/exports_macosx.sym")
endif()
set(INSTALL_DIR ${CMAKE_SOURCE_DIR}/src/main/resources/org/mira/llvm/binding/${OS}/${ARCH})
#if(WINDOWS)
#  install(TARGETS mira-llvm RUNTIME DESTINATION ${INSTALL_DIR})
#else()
#  install(TARGETS mira-llvm LIBRARY DESTINATION ${INSTALL_DIR})
#endif()

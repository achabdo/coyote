# Install script for directory: /home/abdo/miravm/vm/rt/android

# Set the install prefix
if(NOT DEFINED CMAKE_INSTALL_PREFIX)
  set(CMAKE_INSTALL_PREFIX "/usr/local")
endif()
string(REGEX REPLACE "/$" "" CMAKE_INSTALL_PREFIX "${CMAKE_INSTALL_PREFIX}")

# Set the install configuration name.
if(NOT DEFINED CMAKE_INSTALL_CONFIG_NAME)
  if(BUILD_TYPE)
    string(REGEX REPLACE "^[^A-Za-z0-9_]+" ""
           CMAKE_INSTALL_CONFIG_NAME "${BUILD_TYPE}")
  else()
    set(CMAKE_INSTALL_CONFIG_NAME "debug")
  endif()
  message(STATUS "Install configuration: \"${CMAKE_INSTALL_CONFIG_NAME}\"")
endif()

# Set the component getting installed.
if(NOT CMAKE_INSTALL_COMPONENT)
  if(COMPONENT)
    message(STATUS "Install component: \"${COMPONENT}\"")
    set(CMAKE_INSTALL_COMPONENT "${COMPONENT}")
  else()
    set(CMAKE_INSTALL_COMPONENT)
  endif()
endif()

# Install shared libraries without execute permission?
if(NOT DEFINED CMAKE_INSTALL_SO_NO_EXE)
  set(CMAKE_INSTALL_SO_NO_EXE "1")
endif()

if(NOT CMAKE_INSTALL_LOCAL_ONLY)
  # Include the install script for each subdirectory.
  include("/home/abdo/miravm/rt/android/dalvik/cmake_install.cmake")
  include("/home/abdo/miravm/rt/android/libcore/cmake_install.cmake")
  include("/home/abdo/miravm/rt/android/libnativehelper/cmake_install.cmake")
  include("/home/abdo/miravm/rt/android/external/expat/cmake_install.cmake")
  include("/home/abdo/miravm/rt/android/external/fdlibm/cmake_install.cmake")
  include("/home/abdo/miravm/rt/android/external/icu4c/cmake_install.cmake")
  include("/home/abdo/miravm/rt/android/external/javasqlite/cmake_install.cmake")
  include("/home/abdo/miravm/rt/android/external/openssl/cmake_install.cmake")
  include("/home/abdo/miravm/rt/android/external/sqlite/cmake_install.cmake")
  include("/home/abdo/miravm/rt/android/external/zlib/cmake_install.cmake")

endif()


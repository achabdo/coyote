if("4e22546e8a161b6bab93e43c115a22151daf45c5" STREQUAL "")
  message(FATAL_ERROR "Tag for git checkout should not be empty.")
endif()

set(run 0)

if("/home/abdo/miravm/vm/libatomic_ops-prefix/src/libatomic_ops-stamp/libatomic_ops-gitinfo.txt" IS_NEWER_THAN "/home/abdo/miravm/vm/libatomic_ops-prefix/src/libatomic_ops-stamp/libatomic_ops-gitclone-lastrun.txt")
  set(run 1)
endif()

if(NOT run)
  message(STATUS "Avoiding repeated git clone, stamp file is up to date: '/home/abdo/miravm/vm/libatomic_ops-prefix/src/libatomic_ops-stamp/libatomic_ops-gitclone-lastrun.txt'")
  return()
endif()

execute_process(
  COMMAND ${CMAKE_COMMAND} -E remove_directory "/home/abdo/miravm/vm/libatomic_ops-prefix/src/libatomic_ops"
  RESULT_VARIABLE error_code
  )
if(error_code)
  message(FATAL_ERROR "Failed to remove directory: '/home/abdo/miravm/vm/libatomic_ops-prefix/src/libatomic_ops'")
endif()

# try the clone 3 times incase there is an odd git clone issue
set(error_code 1)
set(number_of_tries 0)
while(error_code AND number_of_tries LESS 3)
  execute_process(
    COMMAND "/usr/bin/git" clone --origin "origin" "git://github.com/ivmai/libatomic_ops.git" "libatomic_ops"
    WORKING_DIRECTORY "/home/abdo/miravm/vm/libatomic_ops-prefix/src"
    RESULT_VARIABLE error_code
    )
  math(EXPR number_of_tries "${number_of_tries} + 1")
endwhile()
if(number_of_tries GREATER 1)
  message(STATUS "Had to git clone more than once:
          ${number_of_tries} times.")
endif()
if(error_code)
  message(FATAL_ERROR "Failed to clone repository: 'git://github.com/ivmai/libatomic_ops.git'")
endif()

execute_process(
  COMMAND "/usr/bin/git" checkout 4e22546e8a161b6bab93e43c115a22151daf45c5
  WORKING_DIRECTORY "/home/abdo/miravm/vm/libatomic_ops-prefix/src/libatomic_ops"
  RESULT_VARIABLE error_code
  )
if(error_code)
  message(FATAL_ERROR "Failed to checkout tag: '4e22546e8a161b6bab93e43c115a22151daf45c5'")
endif()

execute_process(
  COMMAND "/usr/bin/git" submodule init 
  WORKING_DIRECTORY "/home/abdo/miravm/vm/libatomic_ops-prefix/src/libatomic_ops"
  RESULT_VARIABLE error_code
  )
if(error_code)
  message(FATAL_ERROR "Failed to init submodules in: '/home/abdo/miravm/vm/libatomic_ops-prefix/src/libatomic_ops'")
endif()

execute_process(
  COMMAND "/usr/bin/git" submodule update --recursive 
  WORKING_DIRECTORY "/home/abdo/miravm/vm/libatomic_ops-prefix/src/libatomic_ops"
  RESULT_VARIABLE error_code
  )
if(error_code)
  message(FATAL_ERROR "Failed to update submodules in: '/home/abdo/miravm/vm/libatomic_ops-prefix/src/libatomic_ops'")
endif()

# Complete success, update the script-last-run stamp file:
#
execute_process(
  COMMAND ${CMAKE_COMMAND} -E copy
    "/home/abdo/miravm/vm/libatomic_ops-prefix/src/libatomic_ops-stamp/libatomic_ops-gitinfo.txt"
    "/home/abdo/miravm/vm/libatomic_ops-prefix/src/libatomic_ops-stamp/libatomic_ops-gitclone-lastrun.txt"
  WORKING_DIRECTORY "/home/abdo/miravm/vm/libatomic_ops-prefix/src/libatomic_ops"
  RESULT_VARIABLE error_code
  )
if(error_code)
  message(FATAL_ERROR "Failed to copy script-last-run stamp file: '/home/abdo/miravm/vm/libatomic_ops-prefix/src/libatomic_ops-stamp/libatomic_ops-gitclone-lastrun.txt'")
endif()


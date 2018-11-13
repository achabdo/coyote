if("0458ff8c3cc04376bb4584de7729e6dbb6384bad" STREQUAL "")
  message(FATAL_ERROR "Tag for git checkout should not be empty.")
endif()

set(run 0)

if("/home/abdo/miravm/vm/extgc-prefix/src/extgc-stamp/extgc-gitinfo.txt" IS_NEWER_THAN "/home/abdo/miravm/vm/extgc-prefix/src/extgc-stamp/extgc-gitclone-lastrun.txt")
  set(run 1)
endif()

if(NOT run)
  message(STATUS "Avoiding repeated git clone, stamp file is up to date: '/home/abdo/miravm/vm/extgc-prefix/src/extgc-stamp/extgc-gitclone-lastrun.txt'")
  return()
endif()

execute_process(
  COMMAND ${CMAKE_COMMAND} -E remove_directory "/home/abdo/miravm/vm/extgc-prefix/src/extgc"
  RESULT_VARIABLE error_code
  )
if(error_code)
  message(FATAL_ERROR "Failed to remove directory: '/home/abdo/miravm/vm/extgc-prefix/src/extgc'")
endif()

# try the clone 3 times incase there is an odd git clone issue
set(error_code 1)
set(number_of_tries 0)
while(error_code AND number_of_tries LESS 3)
  execute_process(
    COMMAND "/usr/bin/git" clone --origin "origin" "git://github.com/robovm/bdwgc.git" "extgc"
    WORKING_DIRECTORY "/home/abdo/miravm/vm/extgc-prefix/src"
    RESULT_VARIABLE error_code
    )
  math(EXPR number_of_tries "${number_of_tries} + 1")
endwhile()
if(number_of_tries GREATER 1)
  message(STATUS "Had to git clone more than once:
          ${number_of_tries} times.")
endif()
if(error_code)
  message(FATAL_ERROR "Failed to clone repository: 'git://github.com/robovm/bdwgc.git'")
endif()

execute_process(
  COMMAND "/usr/bin/git" checkout 0458ff8c3cc04376bb4584de7729e6dbb6384bad
  WORKING_DIRECTORY "/home/abdo/miravm/vm/extgc-prefix/src/extgc"
  RESULT_VARIABLE error_code
  )
if(error_code)
  message(FATAL_ERROR "Failed to checkout tag: '0458ff8c3cc04376bb4584de7729e6dbb6384bad'")
endif()

execute_process(
  COMMAND "/usr/bin/git" submodule init 
  WORKING_DIRECTORY "/home/abdo/miravm/vm/extgc-prefix/src/extgc"
  RESULT_VARIABLE error_code
  )
if(error_code)
  message(FATAL_ERROR "Failed to init submodules in: '/home/abdo/miravm/vm/extgc-prefix/src/extgc'")
endif()

execute_process(
  COMMAND "/usr/bin/git" submodule update --recursive 
  WORKING_DIRECTORY "/home/abdo/miravm/vm/extgc-prefix/src/extgc"
  RESULT_VARIABLE error_code
  )
if(error_code)
  message(FATAL_ERROR "Failed to update submodules in: '/home/abdo/miravm/vm/extgc-prefix/src/extgc'")
endif()

# Complete success, update the script-last-run stamp file:
#
execute_process(
  COMMAND ${CMAKE_COMMAND} -E copy
    "/home/abdo/miravm/vm/extgc-prefix/src/extgc-stamp/extgc-gitinfo.txt"
    "/home/abdo/miravm/vm/extgc-prefix/src/extgc-stamp/extgc-gitclone-lastrun.txt"
  WORKING_DIRECTORY "/home/abdo/miravm/vm/extgc-prefix/src/extgc"
  RESULT_VARIABLE error_code
  )
if(error_code)
  message(FATAL_ERROR "Failed to copy script-last-run stamp file: '/home/abdo/miravm/vm/extgc-prefix/src/extgc-stamp/extgc-gitclone-lastrun.txt'")
endif()


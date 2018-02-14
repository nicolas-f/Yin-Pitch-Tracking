include_directories (src/main/c/)

cmake_minimum_required (VERSION 2.6)

project (yin)

add_library(yin src/main/c/Yin.c)

#------------#
#    TEST 1
#------------#

# Data files
set(TEST_DATA_DIR "${PROJECT_BINARY_DIR}/TestAudio")

# Test executable and folder
set(YIN_TESTS
    src/test/c/Test_Yin.c)


add_executable(TEST_yin ${YIN_TESTS})

target_link_libraries (TEST_yin
    yin)

set_property(TARGET TEST_yin PROPERTY FOLDER "tests")

# Add to test suite
enable_testing()
add_test( NAME yin_test1
    WORKING_DIRECTORY ${TEST_DATA_DIR}
    COMMAND TEST_yin )
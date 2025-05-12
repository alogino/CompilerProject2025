#!/bin/bash

# Auteur : gl13
#
# Context tree test for all deca source files in src/test/deca/context
#
# Expects success for all files in valid subdirectory, and compares the
# resulting context tree to a known correct one if it exists, no comparison
# otherwise.
#
# Expects failure for all files in invalid subdirectory, and compares error
# resulting error message to a known correct one that should always exist.

cd "$(dirname "$0")"/../../.. || exit 1

PATH=./src/test/script/launchers:"$PATH"

RED='\e[31m'
GREEN='\e[32m'
YELLOW='\e[33m'
BLUE='\e[34m'
NC='\e[0m'

# create test_out diretory (and all subdirectories) if it does not already
# exist
mkdir -p test_out/test/deca/context/invalid/assign
mkdir -p test_out/test/deca/context/invalid/initialization
mkdir -p test_out/test/deca/context/invalid/provided
mkdir -p test_out/test/deca/context/invalid/object
mkdir -p test_out/test/deca/context/valid/assign
mkdir -p test_out/test/deca/context/valid/initialization
mkdir -p test_out/test/deca/context/valid/provided
mkdir -p test_out/test/deca/context/valid/class
mkdir -p test_out/test/deca/context/valid/object

valid_test_nb=0
valid_err_nb=0

context_test_valid () {
    echo -e "Context test for ${BLUE}VALID${NC} source files"

    for deca_source in $(find src/test/deca/context/valid -type f -name "*.deca")
    do
        passed=true
        ((valid_test_nb=valid_test_nb+1))

        # information on test execution
        comparison_possible=false

        # output files
        cont_lis="${deca_source%.deca}"-cont.lis
        cont_lis="${cont_lis/src/test_out}"

        cont_res="${deca_source%.deca}"-cont.res
        cont_res="${cont_res/src/test_out}"

        # comparison file
        expected_tree="${deca_source%.deca}"-verif.lis
        expected_tree="${expected_tree/src/verif}"

        test_context "$deca_source" | grep -v "DEBUG" 1> "$cont_lis" 2> "$cont_res"

        # unexpected context errors
        if [ -s "$cont_res" ]; then
            echo -e "${RED}Unexpected context error in file:${NC} ${deca_source}"
            echo -e "${RED}test_context output:${NC}"
            cat "${cont_res}"
            echo -e "\n"
            passed=false
            ((valid_err_nb=valid_err_nb+1))

        # Comparison of the generated context tree with the expected one
        elif [ -f "$expected_tree" ]; then
            comparison_possible=true
            if ! diff -q "$cont_lis" "$expected_tree" > /dev/null; then
                echo -e "${RED}Mismatch in context tree for file:${NC} ${deca_source}"
                echo -e "Expected:"
                cat "$expected_tree"
                echo -e "Got:"
                cat "$cont_lis"
                echo -e "\n"
                passed=false
                ((valid_err_nb=valid_err_nb+1))
            fi
        # else
        #   echo -e "${YELLOW}Missing expected context tree file for:${NC} ${deca_source}"
        fi

        # print individual test result
        if [ "$passed" = true ]; then
            echo -e -n "${GREEN}PASSED: ${NC}"
        else
            echo -e -n "${RED}FAILED: ${NC}"
        fi

        echo -e -n "${deca_source} "
        echo -e -n "[Context tree comparison: "
        if [ "$comparison_possible" = true ]; then
            echo -e  "${GREEN}YES${NC}]"
        else
            echo -e  "${YELLOW}NO${NC}]"
        fi

    done

    if ((valid_err_nb > 0)) 
    then
        echo -e "\n${RED}FAILED${NC} Context test for ${BLUE}VALID${NC} source files"
        echo -e "${RED}${valid_err_nb}${NC}/${valid_test_nb} tests failed. \n"
    else
        echo -e "${GREEN}PASSED${NC} Context test for ${BLUE}VALID${NC} source files \n"
        echo -e "All ${GREEN}${valid_test_nb}${NC} tests passed. \n"
    fi
}

invalid_test_nb=0
invalid_err_nb=0

context_test_invalid () {
    echo -e "Context test for ${BLUE}INVALID${NC} source files"

    for deca_source in $(find src/test/deca/context/invalid -type f -name "*.deca")
    do
        passed=true
        ((invalid_test_nb=invalid_test_nb+1))

        # output file
        cont_res="${deca_source%.deca}"-cont.res
        cont_res="${cont_res/src/test_out}"

        # comparison file
        expected_error="${deca_source%.deca}"-verif.res
        expected_error="${expected_error/src/verif}"

        test_context "$deca_source" 1> /dev/null 2> "$cont_res"

        if [ -f "$expected_error" ]; then
            if ! diff -q "$cont_res" "$expected_error" > /dev/null; then
                echo -e "${RED}Mismatch in expected context error for file:${NC} ${deca_source}"
                echo -e "Expected:"
                cat "$expected_error"
                echo -e "Got:"
                cat "$cont_res"
                echo -e "\n"
                passed=false
                ((invalid_err_nb=invalid_err_nb+1))
            else
                echo -e "${GREEN}Context error correctly caught in file:${NC} ${deca_source}"
            fi
        else
            echo -e "${RED}Missing expected error file for:${NC} ${deca_source}"
            passed=false
            ((invalid_err_nb=invalid_err_nb+1))
        fi
    done

    if ((invalid_err_nb > 0)) 
    then
        echo -e "\n${RED}FAILED${NC} Context test for ${BLUE}INVALID${NC} source files"
        echo -e "${RED}${invalid_err_nb}${NC}/${invalid_test_nb} tests failed. \n"
    else
        echo -e "${GREEN}PASSED${NC} Context test for ${BLUE}INVALID${NC} source files \n"
        echo -e "All ${GREEN}${invalid_test_nb}${NC} tests passed. \n"
    fi
}

context_test_valid
context_test_invalid

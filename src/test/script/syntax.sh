#!/bin/bash

# Auteur : gl13
#
# Syntax tree test for all deca source files in src/test/deca/syntax
#
# Expects success for all files in valid subdirectory, and compares the
# resulting syntax tree to a known correct one if it exists, no comparison
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
mkdir -p test_out/test/deca/syntax/valid/provided
mkdir -p test_out/test/deca/syntax/valid/parallel_compile
mkdir -p test_out/test/deca/syntax/valid/print
mkdir -p test_out/test/deca/syntax/valid/class
mkdir -p test_out/test/deca/syntax/invalid/provided
mkdir -p test_out/test/deca/syntax/invalid/class

valid_test_nb=0
valid_err_nb=0

syntax_test_valid () {
    echo -e "Syntax test for ${BLUE}VALID${NC} source files"

    for deca_source in $(find src/test/deca/syntax/valid -type f -name "*.deca")
    do
        passed=true
        ((valid_test_nb=valid_test_nb+1))

        # information on test execution
        comparison_possible=false

        # output files
        synt_lis="${deca_source%.deca}"-synt.lis
        synt_lis="${synt_lis/src/test_out}"

        synt_res="${deca_source%.deca}"-synt.res
        synt_res="${synt_res/src/test_out}"

        # comparison file
        expected_tree="${deca_source%.deca}"-verif.lis
        expected_tree="${expected_tree/src/verif}"

        test_synt "$deca_source" | grep -v "DEBUG" 1> "$synt_lis" 2> "$synt_res"

        # unexpected syntax errors
        if [ -s "$synt_res" ]; then
            echo -e "${RED}Unexpected syntax error in file:${NC} ${deca_source}"
            echo -e "${RED}test_synt output:${NC}"
            cat "${synt_res}"
            echo -e "\n"
            passed=false
            ((valid_err_nb=valid_err_nb+1))

        # Comparison of the generated syntax tree with the expected one
        elif [ -f "$expected_tree" ]; then
            comparison_possible=true
            if ! diff -q "$synt_lis" "$expected_tree" > /dev/null; then
                echo -e "${RED}Mismatch in syntax tree for file:${NC} ${deca_source}"
                echo -e "Expected:"
                cat "$expected_tree"
                echo -e "Got:"
                cat "$synt_lis"
                echo -e "\n"
                passed=false
                ((valid_err_nb=valid_err_nb+1))
            fi
        # else
        #   echo -e "${YELLOW}Missing expected syntax tree file for:${NC} ${deca_source}"
        fi

        # print individual test result
        if [ "$passed" = true ]; then
            echo -e -n "${GREEN}PASSED: ${NC}"
        else
            echo -e -n "${RED}FAILED: ${NC}"
        fi

        echo -e -n "${deca_source} "
        echo -e -n "[Syntax tree comparison: "
        if [ "$comparison_possible" = true ]; then
            echo -e  "${GREEN}YES${NC}]"
        else
            echo -e  "${YELLOW}NO${NC}]"
        fi

    done

    if ((valid_err_nb > 0)) then
        echo -e "\n${RED}FAILED${NC} Syntax test for ${BLUE}VALID${NC} source files"
        echo -e "${RED}${valid_err_nb}${NC}/${valid_test_nb} tests failed. \n"
    else
        echo -e "${GREEN}PASSED${NC} Syntax test for ${BLUE}VALID${NC} source files \n"
        echo -e "All ${GREEN}${valid_test_nb}${NC} tests passed. \n"
    fi
}

invalid_test_nb=0
invalid_err_nb=0

syntax_test_invalid () {
    echo -e "Syntax test for ${BLUE}INVALID${NC} source files"

    for deca_source in $(find src/test/deca/syntax/invalid -type f -name "*.deca")
    do
        passed=true
        ((invalid_test_nb=invalid_test_nb+1))

        # output file
        synt_res="${deca_source%.deca}"-synt.res
        synt_res="${synt_res/src/test_out}"

        # comparison file
        expected_error="${deca_source%.deca}"-verif.res
        expected_error="${expected_error/src/verif}"

        test_synt "$deca_source" 1> /dev/null 2> "$synt_res"

        if [ -f "$expected_error" ]; then
            if ! diff -q "$synt_res" "$expected_error" > /dev/null; then
                echo -e "${RED}Mismatch in expected syntax error for file:${NC} ${deca_source}"
                echo -e "Expected:"
                cat "$expected_error"
                echo -e "Got:"
                cat "$synt_res"
                echo -e "\n"
                passed=false
                ((invalid_err_nb=invalid_err_nb+1))
            else
                echo -e "${GREEN}Syntax error correctly caught in file:${NC} ${deca_source}"
            fi
        else
            echo -e "${RED}Missing expected error file for:${NC} ${deca_source}"
            passed=false
            ((invalid_err_nb=invalid_err_nb+1))
        fi
    done

    if ((invalid_err_nb > 0)) then
        echo -e "\n${RED}FAILED${NC} Syntax test for ${BLUE}INVALID${NC} source files"
        echo -e "${RED}${invalid_err_nb}${NC}/${invalid_test_nb} tests failed. \n"
    else
        echo -e "${GREEN}PASSED${NC} Syntax test for ${BLUE}INVALID${NC} source files \n"
        echo -e "All ${GREEN}${invalid_test_nb}${NC} tests passed. \n"
    fi
}

syntax_test_valid
syntax_test_invalid

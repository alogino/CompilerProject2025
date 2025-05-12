#!/bin/bash

# Auteur : gl13
#
# IMA assembly test for all deca source files in src/test/deca/codegen
#
# Expects success for all files in valid subdirectory, and compares the
# resulting IMA assembly code to a known correct one if it exists, no
# comparison otherwise.

cd "$(dirname "$0")"/../../.. || exit 1

PATH=./src/test/script/launchers:"$PATH"

RED='\e[31m'
GREEN='\e[32m'
YELLOW='\e[33m'
BLUE='\e[34m'
NC='\e[0m'

# create test_out diretory (and all subdirectories) if it does not already
# exist
mkdir -p test_out/test/deca/codegen/valid
mkdir -p test_out/test/deca/codegen/valid/arithmetique
mkdir -p test_out/test/deca/codegen/valid/assign
mkdir -p test_out/test/deca/codegen/valid/bool
mkdir -p test_out/test/deca/codegen/valid/if
mkdir -p test_out/test/deca/codegen/valid/print
mkdir -p test_out/test/deca/codegen/valid/provided
mkdir -p test_out/test/deca/codegen/valid/unary-minus
mkdir -p test_out/test/deca/codegen/valid/value
mkdir -p test_out/test/deca/codegen/valid/while
mkdir -p test_out/test/deca/codegen/valid/unary-minus
mkdir -p test_out/test/deca/codegen/perf/provided
mkdir -p test_out/test/deca/codegen/valid/assign
mkdir -p test_out/test/deca/codegen/valid/class
mkdir -p test_out/test/deca/codegen/valid/cast
mkdir -p test_out/test/deca/codegen/valid/instanceof

mkdir -p test_out/test/deca/codegen/invalid
mkdir -p test_out/test/deca/codegen/invalid/if
mkdir -p test_out/test/deca/codegen/invalid/arithmetiques
mkdir -p test_out/test/deca/codegen/invalid/logiques
mkdir -p test_out/test/deca/codegen/invalid/loops


valid_test_nb=0
valid_err_nb=0

codegen_test_valid () {
    echo -e "IMA codegen test for ${BLUE}VALID${NC} source files"

    for deca_source in $(find src/test/deca/codegen/valid -type f -name "*.deca")
    do
        passed=true
        ((valid_test_nb=valid_test_nb+1))

        # information on test execution
        comparison_possible=false
        execution_verification=false

        # output files
        codegen_out="${deca_source%.deca}".ass
        
        codegen_res="${deca_source%.deca}"-codegen.res
        codegen_res="${codegen_res/src/test_out}"

        ima_res="${deca_source%.deca}".out
        ima_res="${ima_res/src/test_out}"

        # comparison file
        expected_ass="${deca_source%.deca}"-verif.ass
        expected_ass="${expected_ass/src/verif}"

        # execution verification file
        expected_out="${deca_source%.deca}"-verif.out
        expected_out="${expected_out/src/verif}"

        decac "$deca_source" 2> "$codegen_res"

        # unexpected syntax errors
        if [ -s "$codegen_res" ]; then
            echo -e "${RED}Unexpected compilation error in file:${NC} ${deca_source}"
            echo -e "${RED}decac output:${NC}"
            cat "${codegen_res}"
            echo -e "\n"
            passed=false
            ((valid_err_nb=valid_err_nb+1))

        # Comparison of the generated IMA assembly code with the expected one
        elif [ -f "$expected_ass" ]; then
            comparison_possible=true
            if ! diff -q "$codegen_out" "$expected_ass" > /dev/null; then
                echo -e "${RED}Mismatch in IMA assembly code for file:${NC} ${deca_source}"
                echo -e "Expected:"
                cat "$expected_ass"
                echo -e "Got:"
                cat "$codegen_out"
                echo -e "\n"
                passed=false
                ((valid_err_nb=valid_err_nb+1))
            fi
        # else
        #   echo -e "${YELLOW}Missing expected IMA assembly code file for:${NC} ${deca_source}"
        fi

        # Verification of IMA virtual machine execution
        ima "$codegen_out" > "$ima_res"
        if [ -f "$expected_out" ] && [ "$passed" = true ]; then
            execution_verification=true
            if ! diff -q "$ima_res" "$expected_out" > /dev/null; then
                echo -e "${RED}Mismatch in IMA VM exeuction output for file:${NC} ${deca_source}"
                echo -e "Expected:"
                cat "$expected_out"
                echo -e "Got:"
                cat "$ima_res"
                echo -e "\n"
                passed=false
                ((valid_err_nb=valid_err_nb+1))
            fi
        # else
        #     echo -e "${YELLOW}Missing expected IMA VM output file for:${NC} ${deca_source}"
        fi

        # print individual test result
        if [ "$passed" = true ]; then
            echo -e -n "${GREEN}PASSED: ${NC}"
        else
            echo -e -n "${RED}FAILED: ${NC}"
        fi

        echo -e -n "${deca_source} "
        echo -e -n "[IMA assembly comparison: "
        if [ "$comparison_possible" = true ]; then
            echo -e -n "${GREEN}YES${NC}]"
        else
            echo -e -n "${YELLOW}NO${NC}]"
        fi

        echo -e -n " [IMA VM output comparison: "
        if [ "$execution_verification" = true ]; then
            echo -e "${GREEN}YES${NC}]"
        else
            echo -e "${YELLOW}NO${NC}]"
        fi

    done

    if ((valid_err_nb > 0)) then
        echo -e "\n${RED}FAILED${NC} IMA codegen test for ${BLUE}VALID${NC} source files"
        echo -e "${RED}${valid_err_nb}${NC}/${valid_test_nb} tests failed. \n"
    else
        echo -e "${GREEN}PASSED${NC} IMA codegen test for ${BLUE}VALID${NC} source files \n"
        echo -e "All ${GREEN}${valid_test_nb}${NC} tests passed. \n"
    fi
}

invalid_test_nb=0
invalid_err_nb=0

codegen_test_invalid () {
    echo -e "Codegen test for ${BLUE}INVALID${NC} source files"

    for deca_source in $(find src/test/deca/codegen/invalid -type f -name "*.deca")
    do
        passed=false
        ((invalid_codegen_nb=invalid_codegen_nb+1))

        # Output file
        codegen_out="${deca_source%.deca}".ass

        codegen_res="${deca_source%.deca}-codegen.res"
        codegen_res="${codegen_res/src/test_out}"

        ima_out="${deca_source%.deca}".out
        ima_out="${expected_out/src/test_out}"

        # Expected error file
        expected_codegen_error="${deca_source%.deca}-verif.res"
        expected_codegen_error="${expected_error/src/verif}"

        expected_ima_out="${deca_source%.deca}"-verif.out
        expected_ima_out="${expected_out/src/verif}"
        # Run code generation and redirect error output
        decac "$deca_source" 2> "$codegen_res"

        if [ -f "$expected_codegen_error" ]; then
            if ! diff <(sed 's|^src/||' "$expected_codegen_error") <(sed 's|^.*/src/|src/|' "$codegen_res"); then
            # if ! grep -qF "$(cat "$expected_codegen_error" > /dev/null)" "$codegen_res"; then
                echo -e "${RED}Mismatch in expected codegen error for file:${NC} ${deca_source}"
                echo -e "Expected:"
                cat "$expected_codegen_error"
                echo -e "Got:"
                cat "$codegen_res"
                echo -e "\n"
                passed=false
                ((invalid_codegen_err_nb=invalid_codegen_err_nb+1))
            else
                echo -e "${GREEN}Codegen error correctly caught in file:${NC} ${deca_source}"
            fi
        else
            ima "$codegen_out" > "$ima_out"
            if [ -f "$expected_ima_out" ]; then
                if ! diff -q "$ima_out" "$expected_ima_out" > /dev/null; then
                    echo -e "${RED}Mismatch in expected ima error for file:${NC} ${deca_source}"
                    echo -e "Expected:"
                    cat "$expected_ima_out"
                    echo -e "Got:"
                    cat "$ima_out"
                    echo -e "\n"
                    passed=false
                    ((invalid_codegen_err_nb=invalid_codegen_err_nb+1))
                else
                    echo -e "${GREEN}Codegen error correctly caught in file:${NC} ${deca_source}"
                fi
            else
                echo -e "${RED}Missing expected error file for:${NC} ${deca_source}"
                passed=false
                ((invalid_codegen_err_nb=invalid_codegen_err_nb+1))
            fi
        fi
    done

    if ((invalid_codegen_err_nb > 0))
    then
        echo -e "\n${RED}FAILED${NC} Codegen test for ${BLUE}INVALID${NC} source files"
        echo -e "${RED}${invalid_codegen_err_nb}${NC}/${invalid_codegen_nb} tests failed.\n"
    else
        echo -e "${GREEN}PASSED${NC} Codegen test for ${BLUE}INVALID${NC} source files\n"
        echo -e "All ${GREEN}${invalid_codegen_nb}${NC} tests passed.\n"
    fi
}


# Hi, if you are reading this, you are wondering: why? SO ARE WE.
# WHY MUST WE LIVE IN SUCH A WRETCHED WORLD? WHY MUST YOU BE LIKE THIS, BASH??????
# (coridalement)
rm "./verif/test/deca/codegen/valid/not-verif.out"
echo yeeees > "./verif/test/deca/codegen/valid/not-verif.out"
codegen_test_valid

# just in case...
rm "./verif/test/deca/codegen/valid/not-verif.out"
echo yeeees > "./verif/test/deca/codegen/valid/not-verif.out"

codegen_test_invalid
# uhm... apparently it needs to be here too?
rm "./verif/test/deca/codegen/valid/not-verif.out"
echo yeeees > "./verif/test/deca/codegen/valid/not-verif.out"

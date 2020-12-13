/*
 * Copyright 2019 Web3 Labs Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.web3j.abi;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.StaticArray;
import org.web3j.abi.datatypes.StaticStruct;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Uint;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes16;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.StaticArray2;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Hash;
import org.web3j.utils.Numeric;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FunctionReturnDecoderTest {

    @Test
    public void testSimpleFunctionDecode() {
        Function function =
                new Function(
                        "test",
                        Collections.<Type>emptyList(),
                        Collections.singletonList(new TypeReference<Uint>() {}));

        assertEquals(
                FunctionReturnDecoder.decode(
                        "0x0000000000000000000000000000000000000000000000000000000000000037",
                        function.getOutputParameters()),
                (Collections.singletonList(new Uint(BigInteger.valueOf(55)))));
    }

    @Test
    public void testSimpleFunctionStringResultDecode() {
        Function function =
                new Function(
                        "simple",
                        Arrays.asList(),
                        Collections.singletonList(new TypeReference<Utf8String>() {}));

        List<Type> utf8Strings =
                FunctionReturnDecoder.decode(
                        "0x0000000000000000000000000000000000000000000000000000000000000020"
                                + "000000000000000000000000000000000000000000000000000000000000000d"
                                + "6f6e65206d6f72652074696d6500000000000000000000000000000000000000",
                        function.getOutputParameters());

        assertEquals(utf8Strings.get(0).getValue(), ("one more time"));
    }

    @Test
    public void testFunctionEmptyStringResultDecode() {
        Function function =
                new Function(
                        "test",
                        Collections.emptyList(),
                        Collections.singletonList(new TypeReference<Utf8String>() {}));

        List<Type> utf8Strings =
                FunctionReturnDecoder.decode(
                        "0x0000000000000000000000000000000000000000000000000000000000000020"
                                + "0000000000000000000000000000000000000000000000000000000000000000",
                        function.getOutputParameters());

        assertEquals(utf8Strings.get(0).getValue(), (""));
    }

    @Test
    public void testMultipleResultFunctionDecode() {
        Function function =
                new Function(
                        "test",
                        Collections.<Type>emptyList(),
                        Arrays.asList(new TypeReference<Uint>() {}, new TypeReference<Uint>() {}));

        assertEquals(
                FunctionReturnDecoder.decode(
                        "0x0000000000000000000000000000000000000000000000000000000000000037"
                                + "0000000000000000000000000000000000000000000000000000000000000007",
                        function.getOutputParameters()),
                (Arrays.asList(new Uint(BigInteger.valueOf(55)), new Uint(BigInteger.valueOf(7)))));
    }

    @Test
    public void testDecodeMultipleStringValues() {
        Function function =
                new Function(
                        "function",
                        Collections.<Type>emptyList(),
                        Arrays.asList(
                                new TypeReference<Utf8String>() {},
                                        new TypeReference<Utf8String>() {},
                                new TypeReference<Utf8String>() {},
                                        new TypeReference<Utf8String>() {}));

        assertEquals(
                FunctionReturnDecoder.decode(
                        "0x0000000000000000000000000000000000000000000000000000000000000080"
                                + "00000000000000000000000000000000000000000000000000000000000000c0"
                                + "0000000000000000000000000000000000000000000000000000000000000100"
                                + "0000000000000000000000000000000000000000000000000000000000000140"
                                + "0000000000000000000000000000000000000000000000000000000000000004"
                                + "6465663100000000000000000000000000000000000000000000000000000000"
                                + "0000000000000000000000000000000000000000000000000000000000000004"
                                + "6768693100000000000000000000000000000000000000000000000000000000"
                                + "0000000000000000000000000000000000000000000000000000000000000004"
                                + "6a6b6c3100000000000000000000000000000000000000000000000000000000"
                                + "0000000000000000000000000000000000000000000000000000000000000004"
                                + "6d6e6f3200000000000000000000000000000000000000000000000000000000",
                        function.getOutputParameters()),
                (Arrays.asList(
                        new Utf8String("def1"), new Utf8String("ghi1"),
                        new Utf8String("jkl1"), new Utf8String("mno2"))));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDecodeStaticArrayValue() {
        List<TypeReference<Type>> outputParameters = new ArrayList<>(1);
        outputParameters.add(
                (TypeReference)
                        new TypeReference.StaticArrayTypeReference<StaticArray<Uint256>>(2) {});
        outputParameters.add((TypeReference) new TypeReference<Uint256>() {});

        List<Type> decoded =
                FunctionReturnDecoder.decode(
                        "0x0000000000000000000000000000000000000000000000000000000000000037"
                                + "0000000000000000000000000000000000000000000000000000000000000001"
                                + "000000000000000000000000000000000000000000000000000000000000000a",
                        outputParameters);

        StaticArray2<Uint256> uint256StaticArray2 =
                new StaticArray2<>(
                        new Uint256(BigInteger.valueOf(55)), new Uint256(BigInteger.ONE));

        List<Type> expected = Arrays.asList(uint256StaticArray2, new Uint256(BigInteger.TEN));
        assertEquals(decoded, (expected));
    }

    @Test
    public void testVoidResultFunctionDecode() {
        Function function = new Function("test", Collections.emptyList(), Collections.emptyList());

        assertEquals(
                FunctionReturnDecoder.decode("0x", function.getOutputParameters()),
                (Collections.emptyList()));
    }

    @Test
    public void testEmptyResultFunctionDecode() {
        Function function =
                new Function(
                        "test",
                        Collections.emptyList(),
                        Collections.singletonList(new TypeReference<Uint>() {}));

        assertEquals(
                FunctionReturnDecoder.decode("0x", function.getOutputParameters()),
                (Collections.emptyList()));
    }

    @Test
    public void testDecodeIndexedUint256Value() {
        Uint256 value = new Uint256(BigInteger.TEN);
        String encoded = TypeEncoder.encodeNumeric(value);

        assertEquals(
                FunctionReturnDecoder.decodeIndexedValue(encoded, new TypeReference<Uint256>() {}),
                (value));
    }

    @Test
    public void testDecodeIndexedStringValue() {
        Utf8String string = new Utf8String("some text");
        String encoded = TypeEncoder.encodeString(string);
        String hash = Hash.sha3(encoded);

        assertEquals(
                FunctionReturnDecoder.decodeIndexedValue(hash, new TypeReference<Utf8String>() {}),
                (new Bytes32(Numeric.hexStringToByteArray(hash))));
    }

    @Test
    public void testDecodeIndexedBytes32Value() {
        String rawInput = "0x1234567890123456789012345678901234567890123456789012345678901234";
        byte[] rawInputBytes = Numeric.hexStringToByteArray(rawInput);

        assertEquals(
                FunctionReturnDecoder.decodeIndexedValue(rawInput, new TypeReference<Bytes32>() {}),
                (new Bytes32(rawInputBytes)));
    }

    @Test
    public void testDecodeIndexedBytes16Value() {
        String rawInput = "0x1234567890123456789012345678901200000000000000000000000000000000";
        byte[] rawInputBytes = Numeric.hexStringToByteArray(rawInput.substring(0, 34));

        assertEquals(
                FunctionReturnDecoder.decodeIndexedValue(rawInput, new TypeReference<Bytes16>() {}),
                (new Bytes16(rawInputBytes)));
    }

    @Test
    public void testDecodeIndexedDynamicBytesValue() {
        DynamicBytes bytes = new DynamicBytes(new byte[] {1, 2, 3, 4, 5});
        String encoded = TypeEncoder.encodeDynamicBytes(bytes);
        String hash = Hash.sha3(encoded);

        assertEquals(
                FunctionReturnDecoder.decodeIndexedValue(
                        hash, new TypeReference<DynamicBytes>() {}),
                (new Bytes32(Numeric.hexStringToByteArray(hash))));
    }

    @Test
    public void testDecodeIndexedDynamicArrayValue() {
        DynamicArray<Uint256> array =
                new DynamicArray<>(Uint256.class, new Uint256(BigInteger.TEN));

        String encoded = TypeEncoder.encodeDynamicArray(array);
        String hash = Hash.sha3(encoded);

        assertEquals(
                FunctionReturnDecoder.decodeIndexedValue(
                        hash, new TypeReference<DynamicArray>() {}),
                (new Bytes32(Numeric.hexStringToByteArray(hash))));
    }

    @Test
    public void testDecodeStaticStruct() {
        String rawInput =
                "0x0000000000000000000000000000000000000000000000000000000000000001"
                        + "0000000000000000000000000000000000000000000000000000000000000064";

        assertEquals(
                FunctionReturnDecoder.decode(
                        rawInput, AbiV2TestFixture.getBarFunction.getOutputParameters()),
                Collections.singletonList(
                        new AbiV2TestFixture.Bar(BigInteger.ONE, BigInteger.valueOf(100))));
    }

    @Test
    public void testDecodeDynamicStruct() {
        String rawInput =
                "0x0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000002"
                        + "6964000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000004"
                        + "6e616d6500000000000000000000000000000000000000000000000000000000";

        assertEquals(
                FunctionReturnDecoder.decode(
                        rawInput, AbiV2TestFixture.getFooFunction.getOutputParameters()),
                Collections.singletonList(new AbiV2TestFixture.Foo("id", "name")));
    }

    @Test
    public void testDecodeDynamicStruct2() {
        String rawInput =
                "0x0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000001"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000002"
                        + "6964000000000000000000000000000000000000000000000000000000000000";

        assertEquals(
                FunctionReturnDecoder.decode(
                        rawInput, AbiV2TestFixture.getBozFunction.getOutputParameters()),
                Collections.singletonList(new AbiV2TestFixture.Boz(BigInteger.ONE, "id")));
    }

    @Test
    public void testDecodeStaticStructNested() {
        String rawInput =
                "0x0000000000000000000000000000000000000000000000000000000000000001"
                        + "000000000000000000000000000000000000000000000000000000000000000a"
                        + "0000000000000000000000000000000000000000000000000000000000000001";

        assertEquals(
                FunctionReturnDecoder.decode(
                        rawInput, AbiV2TestFixture.getFuzzFunction.getOutputParameters()),
                Collections.singletonList(
                        new AbiV2TestFixture.Fuzz(
                                new AbiV2TestFixture.Bar(BigInteger.ONE, BigInteger.TEN),
                                BigInteger.ONE)));
    }

    @Test
    public void testDynamicStructNestedEncode() {
        String rawInput =
                "0x0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000002"
                        + "6964000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000004"
                        + "6e616d6500000000000000000000000000000000000000000000000000000000";

        assertEquals(
                FunctionReturnDecoder.decode(
                        rawInput, AbiV2TestFixture.getNuuFunction.getOutputParameters()),
                Collections.singletonList(
                        new AbiV2TestFixture.Nuu(new AbiV2TestFixture.Foo("id", "name"))));
    }

    @Test
    public void testDecodeTupleDynamicStructNested() {
        String rawInput =
                "0x0000000000000000000000000000000000000000000000000000000000000060"
                        + "0000000000000000000000000000000000000000000000000000000000000001"
                        + "000000000000000000000000000000000000000000000000000000000000000a"
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        + "0000000000000000000000000000000000000000000000000000000000000080"
                        + "0000000000000000000000000000000000000000000000000000000000000002"
                        + "6964000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000004"
                        + "6e616d6500000000000000000000000000000000000000000000000000000000";

        assertThrows(
                UnsupportedOperationException.class,
                () ->
                        FunctionReturnDecoder.decode(
                                rawInput,
                                AbiV2TestFixture.getFooBarFunction.getOutputParameters()));

        //        assertEquals(
        //                FunctionReturnDecoder.decode(
        //                        rawInput,
        // AbiV2TestFixture.getFooBarFunction.getOutputParameters()),
        //                Arrays.asList(
        //                        new AbiV2TestFixture.Foo("id", "name"),
        //                        new AbiV2TestFixture.Bar(BigInteger.ONE, BigInteger.TEN)));
    }

    @Test
    public void testDecodeDynamicListStructs() {
        List<org.web3j.abi.datatypes.Address> cTokens =
                Arrays.<Address>asList(
                        new Address(160, "0x5d3a536E4D6DbD6114cc1Ead35777bAB948E3643"));
        String account = "0x99FD1378ca799ED6772Fe7bCDC9B30B389518962";

        final org.web3j.abi.datatypes.Function function =
                new org.web3j.abi.datatypes.Function(
                        "cTokenBalancesAll",
                        Arrays.<Type>asList(
                                new DynamicArray<org.web3j.abi.datatypes.Address>(
                                        org.web3j.abi.datatypes.Address.class, cTokens) {},
                                new org.web3j.abi.datatypes.Address(160, account)),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<DynamicArray<CTokenBalances>>() {}));

        String rawInput =
                "0x0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000001"
                        + "0000000000000000000000005d3a536e4d6dbd6114cc1ead35777bab948e3643"
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000001109e094d05f4fe55b97b7"
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        + "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";

        List<CTokenBalances> results =
                DynamicArray.class
                        .cast(
                                FunctionReturnDecoder.decode(
                                                rawInput, function.getOutputParameters())
                                        .get(0))
                        .getValue();
        assertEquals(
                results,
                Collections.singletonList(
                        new CTokenBalances(
                                "0x5d3a536E4D6DbD6114cc1Ead35777bAB948E3643",
                                Numeric.toBigInt(
                                        "0000000000000000000000000000000000000000000000000000000000000000"),
                                Numeric.toBigInt(
                                        "0000000000000000000000000000000000000000001109e094d05f4fe55b97b7"),
                                Numeric.toBigInt(
                                        "0000000000000000000000000000000000000000000000000000000000000000"),
                                Numeric.toBigInt(
                                        "0000000000000000000000000000000000000000000000000000000000000000"),
                                Numeric.toBigInt(
                                        "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"))));
    }

    @Test
    public void testDecodeDynamicListMultipleStructs() {
        List<org.web3j.abi.datatypes.Address> cTokens =
                Arrays.<Address>asList(
                        new Address(160, "0x39AA39c021dfbaE8faC545936693aC917d5E7563"),
                        new Address(160, "0x5d3a536E4D6DbD6114cc1Ead35777bAB948E3643"));
        String account = "0xF123e9b47aa50265d01cb0b69B2B027Ab8e5470e";

        final org.web3j.abi.datatypes.Function function =
                new org.web3j.abi.datatypes.Function(
                        "cTokenBalancesAll",
                        Arrays.<Type>asList(
                                new DynamicArray<org.web3j.abi.datatypes.Address>(
                                        org.web3j.abi.datatypes.Address.class, cTokens) {},
                                new org.web3j.abi.datatypes.Address(160, account)),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<DynamicArray<CTokenBalances>>() {}));

        String rawInput =
                "0x0000000000000000000000000000000000000000000000000000000000000020"
                        + "0000000000000000000000000000000000000000000000000000000000000002"
                        + "00000000000000000000000039aa39c021dfbae8fac545936693ac917d5e7563"
                        + "0000000000000000000000000000000000000000000000000002c806287396a9"
                        + "00000000000000000000000000000000000000000000000000000028835b0e13"
                        + "00000000000000000000000000000000000000000000000000000026db37bcfd"
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        + "ffffffffffffffffffffffffffffffffffffffffffffffffffffffaaa1d6613d"
                        + "0000000000000000000000005d3a536e4d6dbd6114cc1ead35777bab948e3643"
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        + "0000000000000000000000000000000000000000000000000000000000000000"
                        + "00000000000000000000000000000000000000000000000010bbe2750471f6a6"
                        + "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";

        List<CTokenBalances> results =
                DynamicArray.class
                        .cast(
                                FunctionReturnDecoder.decode(
                                                rawInput, function.getOutputParameters())
                                        .get(0))
                        .getValue();
        assertEquals(
                results,
                Arrays.asList(
                        new CTokenBalances(
                                "0x39AA39c021dfbaE8faC545936693aC917d5E7563",
                                Numeric.toBigInt(
                                        "0000000000000000000000000000000000000000000000000002c806287396a9"),
                                Numeric.toBigInt(
                                        "00000000000000000000000000000000000000000000000000000028835b0e13"),
                                Numeric.toBigInt(
                                        "00000000000000000000000000000000000000000000000000000026db37bcfd"),
                                Numeric.toBigInt(
                                        "0000000000000000000000000000000000000000000000000000000000000000"),
                                Numeric.toBigInt(
                                        "ffffffffffffffffffffffffffffffffffffffffffffffffffffffaaa1d6613d")),
                        new CTokenBalances(
                                "0x5d3a536E4D6DbD6114cc1Ead35777bAB948E3643",
                                Numeric.toBigInt(
                                        "0000000000000000000000000000000000000000000000000000000000000000"),
                                Numeric.toBigInt(
                                        "0000000000000000000000000000000000000000000000000000000000000000"),
                                Numeric.toBigInt(
                                        "0000000000000000000000000000000000000000000000000000000000000000"),
                                Numeric.toBigInt(
                                        "00000000000000000000000000000000000000000000000010bbe2750471f6a6"),
                                Numeric.toBigInt(
                                        "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"))));
    }

    public static class CTokenBalances extends StaticStruct {
        public String cToken;
        public BigInteger balanceOf;
        public BigInteger borrowBalanceCurrent;
        public BigInteger balanceOfUnderlying;
        public BigInteger tokenBalance;
        public BigInteger tokenAllowance;

        public CTokenBalances(
                String cToken,
                BigInteger balanceOf,
                BigInteger borrowBalanceCurrent,
                BigInteger balanceOfUnderlying,
                BigInteger tokenBalance,
                BigInteger tokenAllowance) {
            super(
                    new Address(cToken),
                    new Uint256(balanceOf),
                    new Uint256(borrowBalanceCurrent),
                    new Uint256(balanceOfUnderlying),
                    new Uint256(tokenBalance),
                    new Uint256(tokenAllowance));
            this.cToken = cToken;
            this.balanceOf = balanceOf;
            this.borrowBalanceCurrent = borrowBalanceCurrent;
            this.balanceOfUnderlying = balanceOfUnderlying;
            this.tokenBalance = tokenBalance;
            this.tokenAllowance = tokenAllowance;
        }

        public CTokenBalances(
                Address cToken,
                Uint256 balanceOf,
                Uint256 borrowBalanceCurrent,
                Uint256 balanceOfUnderlying,
                Uint256 tokenBalance,
                Uint256 tokenAllowance) {
            super(
                    cToken,
                    balanceOf,
                    borrowBalanceCurrent,
                    balanceOfUnderlying,
                    tokenBalance,
                    tokenAllowance);
            this.cToken = cToken.getValue();
            this.balanceOf = balanceOf.getValue();
            this.borrowBalanceCurrent = borrowBalanceCurrent.getValue();
            this.balanceOfUnderlying = balanceOfUnderlying.getValue();
            this.tokenBalance = tokenBalance.getValue();
            this.tokenAllowance = tokenAllowance.getValue();
        }
    }
}

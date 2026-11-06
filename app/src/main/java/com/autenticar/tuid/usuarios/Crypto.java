package com.autenticar.tuid.usuarios;

/** Cifrado reversible de texto. La implementacion real usa Android Keystore; los tests una falsa. */
public interface Crypto {
    String encrypt(String plain) throws Exception;

    String decrypt(String encrypted) throws Exception;
}

package com.hez.kim;

import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.bouncycastle.asn1.*;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import org.apache.commons.io.IOUtils;

public class App {
    private static PrivateKeys privateKeys = new PrivateKeys();
    private static Params  params = new Params("",  "","");
    public App(){
    }
    public static void main( String[] args ) throws Exception {
        String payload = generatePayload();
        String derKeySignedUrl =  ""+""  +
                getSign(payload,privateKeys.getDerStringPrivateKey()) ;
        String pemKeySignedUrl = ""+""
        +getSign(payload, privateKeys.getPemStringPrivateKey());
        HttpResponse authResponse = null;
        try{
            authResponse = makePost(pemKeySignedUrl, "", new StringEntity(new Gson().toJson(new PrivateKeys())), "application/x-www-form-urlencoded");
            System.out.println( deserializeResponseObject(authResponse.getEntity()) );
            String accessToken = deserializeResponseObject(authResponse.getEntity()).getAccess_token();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    private static Response deserializeResponseObject(HttpEntity entity) throws IOException {
        String result = IOUtils.toString(entity.getContent(), StandardCharsets.UTF_8);
        entity.getContent().close();
        Gson gson = new Gson();
        System.out.println( result );
        return gson.fromJson(result, Response.class);
    }
    private static HttpResponse makePost (String url, String access_token,StringEntity postingString, String contentType) throws Exception {
        HttpClient httpClient   = HttpClientBuilder.create().build();
        HttpPost     post          = new HttpPost(url);
        post.setEntity(postingString);
        post.setHeader("Content-type", contentType);
        post.setHeader("Authorization", "Bearer "+access_token);
        return httpClient.execute(post);
    }

    public static String  getSign(String payload,String DEROrPEMStringPrivateKey) throws Exception {
        byte[] base64Signature = signSHA256RSA(payload,DEROrPEMStringPrivateKey);
        return payload + "." + Base64.getUrlEncoder().encodeToString(base64Signature);
    }
    private static byte[] signSHA256RSA(String input, String strPk) throws Exception {
        //cleanup the private key to remove both the headers and footers
        String realPK = strPk.replaceAll("-----END PRIVATE KEY-----", "")
                .replaceAll("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll("\n", "");
        //Decode a Base64 encoded private key into a newly-allocated byte array using the Base64 encoding scheme.
        byte[] realPKcs8EncodedBytes = Base64.getDecoder().decode(realPK);
        PKCS8EncodedKeySpec keySpec;
        KeyFactory kf = KeyFactory.getInstance("RSA");
        Signature privateSignature = Signature.getInstance("SHA256withRSA");
        try{
            //Deal with DER
            System.out.println("DER files handled here");
            ASN1EncodableVector v0 = new ASN1EncodableVector();
            v0.add(new ASN1ObjectIdentifier(PKCSObjectIdentifiers.rsaEncryption.getId()));
            v0.add(DERNull.INSTANCE);
            ASN1EncodableVector v = new ASN1EncodableVector();
            v.add(new ASN1Integer(0));
            v.add(new DERSequence(v0));
            v.add(new DEROctetString(realPKcs8EncodedBytes));
            ASN1Sequence seq = new DERSequence(v);
            realPKcs8EncodedBytes = seq.getEncoded("DER");
            keySpec = new PKCS8EncodedKeySpec(realPKcs8EncodedBytes);
            privateSignature.initSign(kf.generatePrivate(keySpec));
        } catch (Exception exception){
            //Deal with PEM
            System.out.println("PEM files handled here");
            realPKcs8EncodedBytes = Base64.getDecoder().decode(realPK);
            keySpec = new PKCS8EncodedKeySpec(realPKcs8EncodedBytes);
            privateSignature.initSign(kf.generatePrivate(keySpec));
        }
        privateSignature.update(input.getBytes("UTF-8"));
        return privateSignature.sign();
    }

    public static String generatePayload() {
        Date tomorrow = new Date();
        int date = (int) (Math.floor((tomorrow.getTime()/1000))+(60 * 5));
        ClaimSet claimSet = new ClaimSet(params.getIss(),params.getSub(),params.getAud(), date);
        Gson gson = new Gson();
        Header header = new Header("RS256");
        String encoded_JWT_Header = Base64.getUrlEncoder().encodeToString(gson.toJson(header).getBytes());
        String encoded_JWT_Claims_Set = Base64.getUrlEncoder().encodeToString(gson.toJson(claimSet).getBytes());
        String existing_string = encoded_JWT_Header + "." + encoded_JWT_Claims_Set;
        return existing_string.replace("=","");
    }
}
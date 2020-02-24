/**
 * Copyright(c) 2015 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.utilidades;

import br.com.casaautomacao.casagold.classes.CSPLog;
import br.com.casaautomacao.casagold.classes.exceptions.CSPException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Métodos de auxilio para linguagem no contexto de rede/web
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 27/10/2016 - 17:13:17
 */
public abstract class CSPUtilidadesLangRede {

    private static Pattern VALID_IPV4_PATTERN = null;
    private static Pattern VALID_IPV6_PATTERN = null;
    private static String[] CACHE_LOCAL_IP;
    private static String[] CACHE_LOCAL_MAC;

    static {
        try {
            VALID_IPV4_PATTERN = Pattern.compile("([0-9a-f]{1,4}:){7}([0-9a-f]){1,4}", Pattern.CASE_INSENSITIVE);
            VALID_IPV6_PATTERN = Pattern.compile("(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])", Pattern.CASE_INSENSITIVE);
        } catch (PatternSyntaxException e) {
            CSPException.register(e);
        }
    }

    /**
     * Retorna o ip local da máquina
     *
     *
     *
     * @return String - Ip da maquina
     * @throws java.net.SocketException
     */
    public static String getLocalIp() throws SocketException {
        final String[] r = getLocalIps();
        if (r != null && r.length > 0) {
            return r[0];
        }
        return null;
    }

    /**
     * Retorna os ip locais da máquina
     *
     * @return String[] - Ip da maquina
     * @throws java.net.SocketException
     */
    public static String[] getLocalIps() throws SocketException {

        if (CACHE_LOCAL_IP != null && CACHE_LOCAL_IP.length > 0) {
            return CACHE_LOCAL_IP;
        }

        final ArrayList<String> r = new ArrayList<>();

        for (Map.Entry<NetworkInterface, InetAddress> e : getNetworkInterfaces().entrySet()) {
            r.add(e.getValue().getHostAddress());
        }

        CACHE_LOCAL_IP = r.toArray(new String[r.size()]);
        return CACHE_LOCAL_IP;
    }

    /**
     * Retorna o MAC da máquina
     *
     * @return String - MAC da máquina
     * @throws java.net.SocketException
     */
    public static String getMac() throws SocketException {
        final String[] r = getMacs();
        if (r != null && r.length > 0) {
            return r[0];
        }
        return null;
    }

    /**
     * Retorna os MAC's da máquina
     *
     * @return String - MAC da máquina
     * @throws java.net.SocketException
     */
    public static String[] getMacs() throws SocketException {

        if (CACHE_LOCAL_MAC != null && CACHE_LOCAL_MAC.length > 0) {
            return CACHE_LOCAL_MAC;
        }

        final ArrayList<String> r = new ArrayList<>();

        for (Map.Entry<NetworkInterface, InetAddress> e : getNetworkInterfaces().entrySet()) {
            byte[] mac = e.getKey().getHardwareAddress();

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }

            r.add(sb.toString());
        }

        CACHE_LOCAL_MAC = r.toArray(new String[r.size()]);
        return CACHE_LOCAL_MAC;
    }

    /**
     * Retorna o IP externo da máquina
     *
     * @return String - Ip externo da maquina
     * @throws MalformedURLException
     * @throws IOException
     */
    public static String getExternalIp() throws MalformedURLException, IOException {
        URL whatismyip = new URL("http://checkip.amazonaws.com");
        BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
        return in.readLine();
    }

    /**
     * Efetua o ping em um host especifico
     *
     * @param host String - Host a ser verificado
     *
     * @return boolean
     */
    public static boolean ping(String host) {

        try {
            if (isLocalAddress(host)) {
                return true;
            } else {
//                    return InetAddress.getByName(host).isReachable(1500);
                return InetAddress.getByName(host).isReachable(5000);
            }
        } catch (Exception ex) {
            CSPException.register(ex);
        }
        return false;
    }

    /**
     * Verifica e retorna se a porta especificada está aberta
     *
     * @param host String - Endereço a ser verificado
     * @param port int - Porta a ser verificada
     * @return boolean
     */
    public static boolean portIsOpen(String host, int port) {
        try {
            Socket s = new Socket();
            s.connect(new InetSocketAddress(host, port));
            s.close();
            s = null;
            return true;
        } catch (Exception ex) {
        }
        return false;
    }

    /**
     * Valida se a string é um endereço local
     *
     * @param ipAddress String - Texto a ser validado
     * @return
     */
    public static boolean isLocalAddress(String ipAddress) throws Exception {
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            return false;
        }
        if (ipAddress.equals("::0") || ipAddress.equals("127.0.0.1") || ipAddress.toLowerCase().equals("localhost")) {
            return true;
        }

        for (String ip : getLocalIps()) {
            if (ipAddress.endsWith(ip)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Atalho para {@link #isHostAddress(java.lang.String) }
     *
     * @param ipAddress String - Texto a ser validado
     * @return
     */
    public static boolean isIpAddress(String ipAddress) {
        return isHostAddress(ipAddress);
    }

    /**
     * Retorna se e um endereço IP/Host. Apenas valida a string
     *
     * @param ipAddress String - Texto a ser validado
     * @return
     */
    public static boolean isHostAddress(String ipAddress) {
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            return false;
        }

        if (ipAddress.equals("::0") || ipAddress.equals("127.0.0.1") || ipAddress.toLowerCase().equals("localhost")) {
            return true;
        }

        Matcher m = VALID_IPV4_PATTERN.matcher(ipAddress);

        if (m.matches()) {
            return true;
        }
        m = VALID_IPV6_PATTERN.matcher(ipAddress);

        return m.matches();
    }

    /**
     * Retorna se o endereço é um endereço de rede privada. Conforme a RFC 3330
     * (https://tools.ietf.org/html/rfc3330)
     *
     * @param ipAddress
     * @return
     */
    public static boolean isPrivateAddress(String ipAddress) {
        if (!isHostAddress(ipAddress)) {
            return false;
        }
        return ipAddress.startsWith("10.")
                || ipAddress.startsWith("172.")
                || ipAddress.startsWith("192.");
    }

    /**
     * Retorna as interfaces(placas) de rede aceitas pelo sistema
     *
     * @return
     * @throws java.net.SocketException
     */
    public static LinkedHashMap<NetworkInterface, InetAddress> getNetworkInterfaces() throws SocketException {

        final LinkedHashMap<NetworkInterface, InetAddress> r = new LinkedHashMap<>();

        final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

        while (interfaces.hasMoreElements()) {
            NetworkInterface current = interfaces.nextElement();

            if (current.isLoopback()) {
                CSPLog.info(CSPUtilidadesLangRede.class, "interfaces>" + current.getDisplayName() + ">ignorada-por:loopback");
                continue;
            } else if (current.getHardwareAddress() == null) {
                CSPLog.info(CSPUtilidadesLangRede.class, "interfaces>" + current.getDisplayName() + ">ignorada-por:no-mac");
                continue;
            } else if (current.isVirtual()) {
                CSPLog.info(CSPUtilidadesLangRede.class, "interfaces>" + current.getDisplayName() + ">ignorada-por:is-virtual");
                continue;
            } else if (!current.isUp()) {
                CSPLog.info(CSPUtilidadesLangRede.class, "interfaces>" + current.getDisplayName() + ">ignorada-por:no-up");
                continue;
            }

            Enumeration<InetAddress> addresses = current.getInetAddresses();

            while (addresses.hasMoreElements()) {

                InetAddress current_addr = addresses.nextElement();

                if (current_addr instanceof Inet4Address && !current_addr.isLoopbackAddress()) {

                    if (current_addr.getHostAddress() != null) {

                        CSPLog.info(CSPUtilidadesLangRede.class, "interfaces>" + current.getDisplayName() + ":" + current_addr.getCanonicalHostName() + ">ok");
                        r.put(current, current_addr);
                        break;
                    } else {
                        CSPLog.info(CSPUtilidadesLangRede.class, "interfaces>" + current.getDisplayName() + ":" + current_addr.getCanonicalHostName() + ">ignorada-por:host-null");
                    }

                } else {
                    CSPLog.info(CSPUtilidadesLangRede.class, "interfaces>" + current.getDisplayName() + ":" + current_addr.getCanonicalHostName() + ">ignorada-por:no-ipv4-ou-loopback");

                }

            }

        }

        return r;
    }

}

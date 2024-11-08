package task3;

public class dummy {
    static int INT = 4;
        /**
         * Convertit un entier en tableau d'octets.
         * @param value L'entier à convertir.
         * @return Un tableau de 4 octets représentant l'entier.
         */
        private static byte[] intToByteArray(int value) {
            return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value
            };
        }
    
        /**
         * Convertit un tableau de 4 octets en entier.
         * @param bytes Le tableau de 4 octets à convertir.
         * @return L'entier correspondant au tableau d'octets.
         */
        private static int byteArrayToInt(byte[] bytes) {
            if (bytes.length != 4) {
                throw new IllegalArgumentException("Le tableau doit contenir exactement 4 octets");
            }
            return ((bytes[0] & 0xFF) << 24) |
                   ((bytes[1] & 0xFF) << 16) |
                   ((bytes[2] & 0xFF) << 8) |
                   (bytes[3] & 0xFF);
        }
        public static void main(String[] args) {

            System.out.println("INT: " + INT);
            byte[] byteARRAY = intToByteArray(INT);
            System.out.println("byteARRAY: " + byteARRAY);
            System.out.println("INT: " + byteArrayToInt(byteARRAY));

        
    }
}

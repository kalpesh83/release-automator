import java.io.File

class ResultHandler(
    val outputFile: File?
) {

    companion object {
        private const val OUTPUT_FILE = "output_file"

        fun get(args: Array<String>): ResultHandler {
            return ResultHandler(getOrCreateResultFile(args))
        }

        private fun getOrCreateResultFile(args: Array<String>): File? {
            val i = args.indexOf(OUTPUT_FILE)
            if (i != -1) {
                val path = args.getOrNull(i + 1) ?: throw IllegalArgumentException("output path is missing!")
                val file = File(path)
                if (file.exists().not()) {
                    file.createNewFile()
                }
                return file
            }
            return null
        }
    }

}
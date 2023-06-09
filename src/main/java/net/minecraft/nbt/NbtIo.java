package net.minecraft.nbt;

import java.io.BufferedOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.util.FastBufferedInputStream;

public class NbtIo
{
    public static CompoundTag readCompressed(File pFile) throws IOException
    {
        InputStream inputstream = new FileInputStream(pFile);
        CompoundTag compoundtag;

        try
        {
            compoundtag = readCompressed(inputstream);
        }
        catch (Throwable throwable1)
        {
            try
            {
                inputstream.close();
            }
            catch (Throwable throwable)
            {
                throwable1.addSuppressed(throwable);
            }

            throw throwable1;
        }

        inputstream.close();
        return compoundtag;
    }

    private static DataInputStream createDecompressorStream(InputStream p_202494_) throws IOException
    {
        return new DataInputStream(new FastBufferedInputStream(new GZIPInputStream(p_202494_)));
    }

    public static CompoundTag readCompressed(InputStream pFile) throws IOException
    {
        DataInputStream datainputstream = createDecompressorStream(pFile);
        CompoundTag compoundtag;

        try
        {
            compoundtag = read(datainputstream, NbtAccounter.UNLIMITED);
        }
        catch (Throwable throwable1)
        {
            if (datainputstream != null)
            {
                try
                {
                    datainputstream.close();
                }
                catch (Throwable throwable)
                {
                    throwable1.addSuppressed(throwable);
                }
            }

            throw throwable1;
        }

        if (datainputstream != null)
        {
            datainputstream.close();
        }

        return compoundtag;
    }

    public static void parseCompressed(File p_202488_, StreamTagVisitor p_202489_) throws IOException
    {
        InputStream inputstream = new FileInputStream(p_202488_);

        try
        {
            parseCompressed(inputstream, p_202489_);
        }
        catch (Throwable throwable1)
        {
            try
            {
                inputstream.close();
            }
            catch (Throwable throwable)
            {
                throwable1.addSuppressed(throwable);
            }

            throw throwable1;
        }

        inputstream.close();
    }

    public static void parseCompressed(InputStream p_202491_, StreamTagVisitor p_202492_) throws IOException
    {
        DataInputStream datainputstream = createDecompressorStream(p_202491_);

        try
        {
            parse(datainputstream, p_202492_);
        }
        catch (Throwable throwable1)
        {
            if (datainputstream != null)
            {
                try
                {
                    datainputstream.close();
                }
                catch (Throwable throwable)
                {
                    throwable1.addSuppressed(throwable);
                }
            }

            throw throwable1;
        }

        if (datainputstream != null)
        {
            datainputstream.close();
        }
    }

    public static void writeCompressed(CompoundTag pCompoundTag, File pFile) throws IOException
    {
        OutputStream outputstream = new FileOutputStream(pFile);

        try
        {
            writeCompressed(pCompoundTag, outputstream);
        }
        catch (Throwable throwable1)
        {
            try
            {
                outputstream.close();
            }
            catch (Throwable throwable)
            {
                throwable1.addSuppressed(throwable);
            }

            throw throwable1;
        }

        outputstream.close();
    }

    public static void writeCompressed(CompoundTag pCompoundTag, OutputStream pFile) throws IOException
    {
        DataOutputStream dataoutputstream = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(pFile)));

        try
        {
            write(pCompoundTag, dataoutputstream);
        }
        catch (Throwable throwable1)
        {
            try
            {
                dataoutputstream.close();
            }
            catch (Throwable throwable)
            {
                throwable1.addSuppressed(throwable);
            }

            throw throwable1;
        }

        dataoutputstream.close();
    }

    public static void write(CompoundTag pCompoundTag, File pOutput) throws IOException
    {
        FileOutputStream fileoutputstream = new FileOutputStream(pOutput);

        try
        {
            DataOutputStream dataoutputstream = new DataOutputStream(fileoutputstream);

            try
            {
                write(pCompoundTag, dataoutputstream);
            }
            catch (Throwable throwable2)
            {
                try
                {
                    dataoutputstream.close();
                }
                catch (Throwable throwable1)
                {
                    throwable2.addSuppressed(throwable1);
                }

                throw throwable2;
            }

            dataoutputstream.close();
        }
        catch (Throwable throwable3)
        {
            try
            {
                fileoutputstream.close();
            }
            catch (Throwable throwable)
            {
                throwable3.addSuppressed(throwable);
            }

            throw throwable3;
        }

        fileoutputstream.close();
    }

    @Nullable
    public static CompoundTag read(File pInput) throws IOException
    {
        if (!pInput.exists())
        {
            return null;
        }
        else
        {
            FileInputStream fileinputstream = new FileInputStream(pInput);
            CompoundTag compoundtag;

            try
            {
                DataInputStream datainputstream = new DataInputStream(fileinputstream);

                try
                {
                    compoundtag = read(datainputstream, NbtAccounter.UNLIMITED);
                }
                catch (Throwable throwable2)
                {
                    try
                    {
                        datainputstream.close();
                    }
                    catch (Throwable throwable1)
                    {
                        throwable2.addSuppressed(throwable1);
                    }

                    throw throwable2;
                }

                datainputstream.close();
            }
            catch (Throwable throwable3)
            {
                try
                {
                    fileinputstream.close();
                }
                catch (Throwable throwable)
                {
                    throwable3.addSuppressed(throwable);
                }

                throw throwable3;
            }

            fileinputstream.close();
            return compoundtag;
        }
    }

    public static CompoundTag read(DataInput pInput) throws IOException
    {
        return read(pInput, NbtAccounter.UNLIMITED);
    }

    public static CompoundTag read(DataInput pInput, NbtAccounter pAccounter) throws IOException
    {
        Tag tag = readUnnamedTag(pInput, 0, pAccounter);

        if (tag instanceof CompoundTag)
        {
            return (CompoundTag)tag;
        }
        else
        {
            throw new IOException("Root tag must be a named compound tag");
        }
    }

    public static void write(CompoundTag pCompoundTag, DataOutput pOutput) throws IOException
    {
        writeUnnamedTag(pCompoundTag, pOutput);
    }

    public static void parse(DataInput p_197510_, StreamTagVisitor p_197511_) throws IOException
    {
        TagType<?> tagtype = TagTypes.getType(p_197510_.readByte());

        if (tagtype == EndTag.TYPE)
        {
            if (p_197511_.visitRootEntry(EndTag.TYPE) == StreamTagVisitor.ValueResult.CONTINUE)
            {
                p_197511_.visitEnd();
            }
        }
        else
        {
            switch (p_197511_.visitRootEntry(tagtype))
            {
                case HALT:
                default:
                    break;

                case BREAK:
                    StringTag.skipString(p_197510_);
                    tagtype.skip(p_197510_);
                    break;

                case CONTINUE:
                    StringTag.skipString(p_197510_);
                    tagtype.parse(p_197510_, p_197511_);
            }
        }
    }

    public static void writeUnnamedTag(Tag pTag, DataOutput pOutput) throws IOException
    {
        pOutput.writeByte(pTag.getId());

        if (pTag.getId() != 0)
        {
            pOutput.writeUTF("");
            pTag.write(pOutput);
        }
    }

    private static Tag readUnnamedTag(DataInput pInput, int pDepth, NbtAccounter pAccounter) throws IOException
    {
        byte b0 = pInput.readByte();

        if (b0 == 0)
        {
            return EndTag.INSTANCE;
        }
        else
        {
            StringTag.skipString(pInput);

            try
            {
                return TagTypes.getType(b0).load(pInput, pDepth, pAccounter);
            }
            catch (IOException ioexception)
            {
                CrashReport crashreport = CrashReport.forThrowable(ioexception, "Loading NBT data");
                CrashReportCategory crashreportcategory = crashreport.addCategory("NBT Tag");
                crashreportcategory.setDetail("Tag type", b0);
                throw new ReportedException(crashreport);
            }
        }
    }
}

/*
* Copyright (c) 2012 The Broad Institute
* 
* Permission is hereby granted, free of charge, to any person
* obtaining a copy of this software and associated documentation
* files (the "Software"), to deal in the Software without
* restriction, including without limitation the rights to use,
* copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the
* Software is furnished to do so, subject to the following
* conditions:
* 
* The above copyright notice and this permission notice shall be
* included in all copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
* OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
* NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
* HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
* WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
* THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package org.broadinstitute.sting.gatk;

import net.sf.picard.PicardException;
import net.sf.samtools.SAMException;

import org.apache.log4j.Logger;
import org.broad.tribble.TribbleException;
import org.broadinstitute.sting.commandline.Argument;
import org.broadinstitute.sting.commandline.ArgumentCollection;
import org.broadinstitute.sting.commandline.CommandLineProgram;
import org.broadinstitute.sting.gatk.arguments.GATKArgumentCollection;
import org.broadinstitute.sting.gatk.refdata.tracks.FeatureManager;
import org.broadinstitute.sting.gatk.walkers.Attribution;
import org.broadinstitute.sting.gatk.walkers.Walker;
import org.broadinstitute.sting.utils.exceptions.UserException;
import org.broadinstitute.sting.utils.help.*;
import org.broadinstitute.sting.utils.text.TextFormattingUtils;

import java.util.*;

/**
 * All command line parameters accepted by all tools in the GATK.
 *
 * The GATK engine itself.  Manages map/reduce data access and runs walkers.
 *
 * We run command line GATK programs using this class.  It gets the command line args, parses them, and hands the
 * gatk all the parsed out information.  Pretty much anything dealing with the underlying system should go here,
 * the gatk engine should  deal with any data related information.
 */
@DocumentedGATKFeature(groupName = HelpConstants.DOCS_CAT_ENGINE)
public class CommandLineGATK extends CommandLineExecutable {
	private static final Logger logger = Logger.getLogger(CommandLineGATK.class);
	
    @Argument(fullName = "analysis_type", shortName = "T", doc = "Type of analysis to run")
    private String analysisName = null;

    // our argument collection, the collection of command line args we accept
    @ArgumentCollection
    private GATKArgumentCollection argCollection = new GATKArgumentCollection();

    /**
     * Get pleasing info about the GATK.
     *
     * @return A list of Strings that contain pleasant info about the GATK.
     */
    @Override
    protected ApplicationDetails getApplicationDetails() {
        return new ApplicationDetails(createApplicationHeader(),
                getAttribution(),
                ApplicationDetails.createDefaultRunningInstructions(getClass()),
                getAdditionalHelp());
    }

    @Override
    public String getAnalysisName() {
        return analysisName;
    }

    @Override
    protected GATKArgumentCollection getArgumentCollection() {
        return argCollection;
    }

    /**
     * Required main method implementation.
     */
    public static void main(String[] argv) {
    	System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        try {
            CommandLineGATK instance = new CommandLineGATK();
            start(instance, argv);
            //System.exit(CommandLineProgram.result); // todo -- this is a painful hack
        } catch (UserException e) {
        	logger.error(e.getMessage());
            //exitSystemWithUserError(e);
        } catch (TribbleException e) {
        	logger.error(e.getMessage());
            // We can generate Tribble Exceptions in weird places when e.g. VCF genotype fields are
            //   lazy loaded, so they aren't caught elsewhere and made into User Exceptions
            //exitSystemWithUserError(e);
        } catch(PicardException e) {
        	logger.error(e.getMessage());
            // TODO: Should Picard exceptions be, in general, UserExceptions or ReviewedStingExceptions?
           // exitSystemWithError(e);
        } catch (SAMException e) {
        	logger.error(e.getMessage());
            checkForMaskedUserErrors(e);
           // exitSystemWithSamError(e);
        } catch (OutOfMemoryError e) {
        	logger.error(e.getMessage());
            //exitSystemWithUserError(new UserException.NotEnoughMemory());
        } catch (Throwable t) {
        	logger.error(t.getMessage());
            checkForMaskedUserErrors(t);
           // exitSystemWithError(t);
        }
    }

    public static final String PICARD_TEXT_SAM_FILE_ERROR_1 = "Cannot use index file with textual SAM file";
    public static final String PICARD_TEXT_SAM_FILE_ERROR_2 = "Cannot retrieve file pointers within SAM text files";
    public static final String NO_SPACE_LEFT_ON_DEVICE_ERROR = "No space left on device";
    public static final String DISK_QUOTA_EXCEEDED_ERROR = "Disk quota exceeded";

    private static void checkForMaskedUserErrors(final Throwable t) {
        // masked out of memory error
        if ( t instanceof OutOfMemoryError )
            exitSystemWithUserError(new UserException.NotEnoughMemory());
        // masked user error
        if ( t instanceof UserException || t instanceof TribbleException )
            exitSystemWithUserError(new UserException(t.getMessage()));

        // no message means no masked error
        final String message = t.getMessage();
        if ( message == null )
            return;

        // too many open files error
        if ( message.contains("Too many open files") )
            exitSystemWithUserError(new UserException.TooManyOpenFiles());

        // malformed BAM looks like a SAM file
        if ( message.contains(PICARD_TEXT_SAM_FILE_ERROR_1) || message.contains(PICARD_TEXT_SAM_FILE_ERROR_2) )
            exitSystemWithSamError(t);

        // can't close tribble index when writing
        if ( message.contains("Unable to close index for") )
            exitSystemWithUserError(new UserException(t.getCause() == null ? message : t.getCause().getMessage()));

        // disk is full
        if ( message.contains(NO_SPACE_LEFT_ON_DEVICE_ERROR) || message.contains(DISK_QUOTA_EXCEEDED_ERROR) )
            exitSystemWithUserError(new UserException.NoSpaceOnDevice());

        // masked error wrapped in another one
        if ( t.getCause() != null )
            checkForMaskedUserErrors(t.getCause());
    }

    /**
     * Creates the a short blurb about the GATK, copyright info, and where to get documentation.
     *
     * @return The application header.
     */
    public static List<String> createApplicationHeader() {
        List<String> header = new ArrayList<String>();
        header.add(String.format("The Genome Analysis Toolkit (GATK) v%s, Compiled %s",getVersionNumber(), getBuildTime()));
        header.add("Copyright (c) 2010 The Broad Institute");
        header.add("For support and documentation go to " + HelpConstants.BASE_GATK_URL);
        return header;
    }

    public static String getVersionNumber() {
        ResourceBundle headerInfo = TextFormattingUtils.loadResourceBundle("StingText");
        return headerInfo.containsKey("org.broadinstitute.sting.gatk.version") ? headerInfo.getString("org.broadinstitute.sting.gatk.version") : "<unknown>";
    }

    public static String getBuildTime() {
        ResourceBundle headerInfo = TextFormattingUtils.loadResourceBundle("StingText");
        return headerInfo.containsKey("build.timestamp") ? headerInfo.getString("build.timestamp") : "<unknown>";
    }

    /**
     * If the user supplied any additional attribution, return it here.
     * @return Additional attribution if supplied by the user.  Empty (non-null) list otherwise.
     */
    private List<String> getAttribution() {
        List<String> attributionLines = new ArrayList<String>();

        // If no analysis name is present, fill in extra help on the walkers.
        WalkerManager walkerManager = engine.getWalkerManager();
        String analysisName = getAnalysisName();
        if(analysisName != null && walkerManager.exists(analysisName)) {
            Class<? extends Walker> walkerType = walkerManager.getWalkerClassByName(analysisName);
            if(walkerType.isAnnotationPresent(Attribution.class))
                attributionLines.addAll(Arrays.asList(walkerType.getAnnotation(Attribution.class).value()));
        }
        return attributionLines;
    }

    /**
     * Retrieves additional information about GATK walkers.
     * the code in HelpFormatter and supply it as a helper to this method.
     *
     * @return A string summarizing the walkers available in this distribution.
     */
    private String getAdditionalHelp() {
        String additionalHelp;

        // If no analysis name is present, fill in extra help on the walkers.
        WalkerManager walkerManager = engine.getWalkerManager();
        String analysisName = getAnalysisName();
        if(analysisName != null && walkerManager.exists(getAnalysisName()))
            additionalHelp = getWalkerHelp(walkerManager.getWalkerClassByName(getAnalysisName()));
        else
            additionalHelp = getAllWalkerHelp();

        return additionalHelp;
    }

    private static final int PACKAGE_INDENT = 1;
    private static final int WALKER_INDENT = 3;
    private static final String FIELD_SEPARATOR = "  ";

    private String getWalkerHelp(Class<? extends Walker> walkerType) {
        // Construct a help string to output details on this walker.
        StringBuilder additionalHelp = new StringBuilder();
        Formatter formatter = new Formatter(additionalHelp);

        formatter.format("Available Reference Ordered Data types:%n");
        formatter.format(new FeatureManager().userFriendlyListOfAvailableFeatures());
        formatter.format("%n");

        formatter.format("For a full description of this walker, see its GATKdocs at:%n");
        formatter.format("%s%n", GATKDocUtils.helpLinksToGATKDocs(walkerType));

        return additionalHelp.toString();
    }

    /**
     * Load in additional help information about all available walkers.
     * @return A string representation of the additional help.
     */
    private String getAllWalkerHelp() {
        // Construct a help string to output available walkers.
        StringBuilder additionalHelp = new StringBuilder();
        Formatter formatter = new Formatter(additionalHelp);

        // Get the list of walker names from the walker manager.
        WalkerManager walkerManager = engine.getWalkerManager();

        // Build a list sorted by walker display name.  As this information is collected, keep track of the longest
        // package / walker name for later formatting.
        SortedSet<HelpEntry> helpText = new TreeSet<HelpEntry>(new HelpEntryComparator());
        
        int longestPackageName = 0;
        int longestWalkerName = 0;
        for(Map.Entry<String,Collection<Class<? extends Walker>>> walkersByPackage: walkerManager.getWalkerNamesByPackage(true).entrySet()) {
            // Get the display name.
            String packageName = walkersByPackage.getKey();
            String packageDisplayName = walkerManager.getPackageDisplayName(walkersByPackage.getKey());
            String packageHelpText = walkerManager.getPackageSummaryText(packageName);

            // Compute statistics about which names is longest.
            longestPackageName = Math.max(longestPackageName,packageDisplayName.length());

            SortedSet<HelpEntry> walkersInPackage = new TreeSet<HelpEntry>(new HelpEntryComparator());
            for(Class<? extends Walker> walkerType: walkersByPackage.getValue()) {
                String walkerName = walkerType.getName();
                String walkerDisplayName = walkerManager.getName(walkerType);
                String walkerHelpText = walkerManager.getWalkerSummaryText(walkerType);                

                longestWalkerName = Math.max(longestWalkerName,walkerManager.getName(walkerType).length());

                walkersInPackage.add(new HelpEntry(walkerName,walkerDisplayName,walkerHelpText));
            }

            // Dump the walkers into the sorted set.
            helpText.add(new HelpEntry(packageName,packageDisplayName,packageHelpText,Collections.unmodifiableSortedSet(walkersInPackage)));
        }

        final int headerWidth = Math.max(longestPackageName+PACKAGE_INDENT,longestWalkerName+WALKER_INDENT);


        for(HelpEntry packageHelp: helpText) {
            printDescriptorLine(formatter,PACKAGE_INDENT,packageHelp.displayName,headerWidth,FIELD_SEPARATOR,packageHelp.summary,TextFormattingUtils.DEFAULT_LINE_WIDTH);
            
            for(HelpEntry walkerHelp: packageHelp.children)
                printDescriptorLine(formatter,WALKER_INDENT,walkerHelp.displayName,headerWidth,FIELD_SEPARATOR,walkerHelp.summary,TextFormattingUtils.DEFAULT_LINE_WIDTH);

            // Print a blank line between sets of walkers.
            printDescriptorLine(formatter,0,"",headerWidth,FIELD_SEPARATOR,"", TextFormattingUtils.DEFAULT_LINE_WIDTH);
        }

        return additionalHelp.toString();
    }

    private void printDescriptorLine(Formatter formatter,
                                     int headerIndentWidth,
                                     String header,
                                     int headerWidth,
                                     String fieldSeparator,
                                     String description,
                                     int lineWidth) {
        final int headerPaddingWidth = headerWidth - header.length() - headerIndentWidth;
        final int descriptionWidth = lineWidth - fieldSeparator.length() - headerWidth;
        List<String> wordWrappedText = TextFormattingUtils.wordWrap(description,descriptionWidth);

        String headerIndentFormatString  = headerIndentWidth  > 0 ? "%" + headerIndentWidth  + "s" : "%s";
        String headerPaddingFormatString = headerPaddingWidth > 0 ? "%" + headerPaddingWidth + "s" : "%s";
        String headerWidthFormatString   = headerWidth        > 0 ? "%" + headerWidth        + "s" : "%s";

        // Output description line.
        formatter.format(headerIndentFormatString + "%s" + headerPaddingFormatString + "%s%s%n",
                "", header, "", fieldSeparator, wordWrappedText.size()>0?wordWrappedText.get(0):"");
        for(int i = 1; i < wordWrappedText.size(); i++)
            formatter.format(headerWidthFormatString + "%s%s%n", "", fieldSeparator, wordWrappedText.get(i));
    }

}

/**
 * Represents a given help entry; contains a display name, a summary and optionally some children.
 */
class HelpEntry {
    public final String uid;
    public final String displayName;
    public final String summary;
    public final SortedSet<HelpEntry> children;

    /**
     * Create a new help entry with the given display name, summary and children.
     * @param uid a unique identifier.  Usually, the java package.
     * @param displayName display name for this help entry.
     * @param summary summary for this help entry.
     * @param children children for this help entry.
     */
    public HelpEntry(String uid, String displayName, String summary, SortedSet<HelpEntry> children)  {
        this.uid = uid;
        this.displayName = displayName;
        this.summary = summary;
        this.children = children;
    }

    /**
     * Create a new help entry with the given display name, summary and children.
     * @param uid a unique identifier.  Usually, the java package.
     * @param displayName display name for this help entry.
     * @param summary summary for this help entry.
     */
    public HelpEntry(String uid, String displayName, String summary) {
        this(uid,displayName,summary,null);
    }

}

/**
 * Compare two help entries by display name.
 */
class HelpEntryComparator implements Comparator<HelpEntry> {
    private static TextFormattingUtils.CaseInsensitiveComparator textComparator = new TextFormattingUtils.CaseInsensitiveComparator();

    /**
     * Compares the order of lhs to rhs, not taking case into account.
     * @param lhs First object to compare.
     * @param rhs Second object to compare.
     * @return 0 if objects are identical; -1 if lhs is before rhs, 1 if rhs is before lhs.  Nulls are treated as after everything else.
     */
    public int compare(HelpEntry lhs, HelpEntry rhs) {
        if(lhs == null && rhs == null) return 0;
        if(lhs == null || lhs.displayName.equals("")) return 1;
        if(rhs == null || rhs.displayName.equals("")) return -1;
        return lhs.displayName.equals(rhs.displayName) ? textComparator.compare(lhs.uid,rhs.uid) : textComparator.compare(lhs.displayName,rhs.displayName);
    }


}
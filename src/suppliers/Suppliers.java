package suppliers;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

/**
 *
 * @author AMaslowiec
 */
@Entity
@Table(name = "suppliers", catalog = "ELRO", schema = "")
@NamedQueries({
    @NamedQuery(name = "Suppliers.findAll", query = "SELECT s FROM Suppliers s"),
    @NamedQuery(name = "Suppliers.findBySupplier", query = "SELECT s FROM Suppliers s WHERE s.supplier = :supplier"),
    @NamedQuery(name = "Suppliers.findByFOB", query = "SELECT s FROM Suppliers s WHERE s.FOB = :FOB"),
    @NamedQuery(name = "Suppliers.findByOfficeName", query = "SELECT s FROM Suppliers s WHERE s.officeName = :officeName"),
    @NamedQuery(name = "Suppliers.findByOfficeVendor", query = "SELECT s FROM Suppliers s WHERE s.officeVendor = :OfficeVendor"),
    @NamedQuery(name = "Suppliers.findByOfficeVendorSFE", query = "SELECT s FROM Suppliers s WHERE s.officeVendorSFE = :OfficeVendorSFE"),
    @NamedQuery(name = "Suppliers.findByOfficeName1", query = "SELECT s FROM Suppliers s WHERE s.officeName1 = :officeName1"),
    @NamedQuery(name = "Suppliers.findByOfficeName2", query = "SELECT s FROM Suppliers s WHERE s.officeName2 = :officeName2"),
    @NamedQuery(name = "Suppliers.findByOfficeAddress1", query = "SELECT s FROM Suppliers s WHERE s.officeAddress1 = :officeAddress1"),
    @NamedQuery(name = "Suppliers.findByOfficeAddress2", query = "SELECT s FROM Suppliers s WHERE s.officeAddress2 = :officeAddress2"),
    @NamedQuery(name = "Suppliers.findByOfficeAddress3", query = "SELECT s FROM Suppliers s WHERE s.officeAddress3 = :officeAddress3"),
    @NamedQuery(name = "Suppliers.findByOfficeCity", query = "SELECT s FROM Suppliers s WHERE s.officeCity = :officeCity"),
    @NamedQuery(name = "Suppliers.findByOfficeProvince", query = "SELECT s FROM Suppliers s WHERE s.officeProvince = :officeProvince"),
    @NamedQuery(name = "Suppliers.findByOfficeCountry", query = "SELECT s FROM Suppliers s WHERE s.officeCountry = :officeCountry"),
    @NamedQuery(name = "Suppliers.findByOfficeZip", query = "SELECT s FROM Suppliers s WHERE s.officeZip = :officeZip"),
    @NamedQuery(name = "Suppliers.findByOfficeWww", query = "SELECT s FROM Suppliers s WHERE s.officeWww = :officeWww"),
    @NamedQuery(name = "Suppliers.findByDBID", query = "SELECT s FROM Suppliers s WHERE s.dbID = :dbID"),
    @NamedQuery(name = "Suppliers.findByFactory1Name", query = "SELECT s FROM Suppliers s WHERE s.factory1Name = :factory1Name"),
    @NamedQuery(name = "Suppliers.findByFactory1Name1", query = "SELECT s FROM Suppliers s WHERE s.factory1Name1 = :factory1Name1"),
    @NamedQuery(name = "Suppliers.findByFactory1Name2", query = "SELECT s FROM Suppliers s WHERE s.factory1Name2 = :factory1Name2"),
    @NamedQuery(name = "Suppliers.findByFactory1Address1", query = "SELECT s FROM Suppliers s WHERE s.factory1Address1 = :factory1Address1"),
    @NamedQuery(name = "Suppliers.findByFactory1Address2", query = "SELECT s FROM Suppliers s WHERE s.factory1Address2 = :factory1Address2"),
    @NamedQuery(name = "Suppliers.findByFactory1Address3", query = "SELECT s FROM Suppliers s WHERE s.factory1Address3 = :factory1Address3"),
    @NamedQuery(name = "Suppliers.findByFactory1City", query = "SELECT s FROM Suppliers s WHERE s.factory1City = :factory1City"),
    @NamedQuery(name = "Suppliers.findByFactory1Province", query = "SELECT s FROM Suppliers s WHERE s.factory1Province = :factory1Province"),
    @NamedQuery(name = "Suppliers.findByFactory1Country", query = "SELECT s FROM Suppliers s WHERE s.factory1Country = :factory1Country"),
    @NamedQuery(name = "Suppliers.findByFactory1Zip", query = "SELECT s FROM Suppliers s WHERE s.factory1Zip = :factory1Zip"),
    @NamedQuery(name = "Suppliers.findByFactory1Www", query = "SELECT s FROM Suppliers s WHERE s.factory1Www = :factory1Www"),
    @NamedQuery(name = "Suppliers.findByDBID1", query = "SELECT s FROM Suppliers s WHERE s.dbID1 = :dbID1"),
    @NamedQuery(name = "Suppliers.findByFactory2Name", query = "SELECT s FROM Suppliers s WHERE s.factory2Name = :factory2Name"),
    @NamedQuery(name = "Suppliers.findByFactory2Name1", query = "SELECT s FROM Suppliers s WHERE s.factory2Name1 = :factory2Name1"),
    @NamedQuery(name = "Suppliers.findByFactory2Name2", query = "SELECT s FROM Suppliers s WHERE s.factory2Name2 = :factory2Name2"),
    @NamedQuery(name = "Suppliers.findByFactory2Address1", query = "SELECT s FROM Suppliers s WHERE s.factory2Address1 = :factory2Address1"),
    @NamedQuery(name = "Suppliers.findByFactory2Address2", query = "SELECT s FROM Suppliers s WHERE s.factory2Address2 = :factory2Address2"),
    @NamedQuery(name = "Suppliers.findByFactory2Address3", query = "SELECT s FROM Suppliers s WHERE s.factory2Address3 = :factory2Address3"),
    @NamedQuery(name = "Suppliers.findByFactory2City", query = "SELECT s FROM Suppliers s WHERE s.factory2City = :factory2City"),
    @NamedQuery(name = "Suppliers.findByFactory2Province", query = "SELECT s FROM Suppliers s WHERE s.factory2Province = :factory2Province"),
    @NamedQuery(name = "Suppliers.findByFactory2Country", query = "SELECT s FROM Suppliers s WHERE s.factory2Country = :factory2Country"),
    @NamedQuery(name = "Suppliers.findByFactory2Zip", query = "SELECT s FROM Suppliers s WHERE s.factory2Zip = :factory2Zip"),
    @NamedQuery(name = "Suppliers.findByFactory2Www", query = "SELECT s FROM Suppliers s WHERE s.factory2Www = :factory2Www"),
    @NamedQuery(name = "Suppliers.findByDBID2", query = "SELECT s FROM Suppliers s WHERE s.dbID2 = :dbID2"),
    @NamedQuery(name = "Suppliers.findByFactory3Name", query = "SELECT s FROM Suppliers s WHERE s.factory3Name = :factory3Name"),
    @NamedQuery(name = "Suppliers.findByFactory3Name1", query = "SELECT s FROM Suppliers s WHERE s.factory3Name1 = :factory3Name1"),
    @NamedQuery(name = "Suppliers.findByFactory3Name2", query = "SELECT s FROM Suppliers s WHERE s.factory3Name2 = :factory3Name2"),
    @NamedQuery(name = "Suppliers.findByFactory3Address1", query = "SELECT s FROM Suppliers s WHERE s.factory3Address1 = :factory3Address1"),
    @NamedQuery(name = "Suppliers.findByFactory3Address2", query = "SELECT s FROM Suppliers s WHERE s.factory3Address2 = :factory3Address2"),
    @NamedQuery(name = "Suppliers.findByFactory3Address3", query = "SELECT s FROM Suppliers s WHERE s.factory3Address3 = :factory3Address3"),    
    @NamedQuery(name = "Suppliers.findByFactory3City", query = "SELECT s FROM Suppliers s WHERE s.factory3City = :factory3City"),
    @NamedQuery(name = "Suppliers.findByFactory3Province", query = "SELECT s FROM Suppliers s WHERE s.factory3Province = :factory3Province"),
    @NamedQuery(name = "Suppliers.findByFactory3Country", query = "SELECT s FROM Suppliers s WHERE s.factory3Country = :factory3Country"),
    @NamedQuery(name = "Suppliers.findByFactory3Zip", query = "SELECT s FROM Suppliers s WHERE s.factory3Zip = :factory3Zip"),
    @NamedQuery(name = "Suppliers.findByFactory3Www", query = "SELECT s FROM Suppliers s WHERE s.factory3Www = :factory3Www"),
    @NamedQuery(name = "Suppliers.findByDBID3", query = "SELECT s FROM Suppliers s WHERE s.dbID3 = :dbID3"),
    @NamedQuery(name = "Suppliers.findBynote", query = "SELECT s FROM Suppliers s WHERE s.note = :note"),
    @NamedQuery(name = "Suppliers.findByContact1Name", query = "SELECT s FROM Suppliers s WHERE s.contact1Name = :contact1Name"),
    @NamedQuery(name = "Suppliers.findByContact1Email", query = "SELECT s FROM Suppliers s WHERE s.contact1Email = :contact1Email"),
    @NamedQuery(name = "Suppliers.findByContact1Phone", query = "SELECT s FROM Suppliers s WHERE s.contact1Phone = :contact1Phone"),
    @NamedQuery(name = "Suppliers.findByContact1Function", query = "SELECT s FROM Suppliers s WHERE s.contact1Function = :contact1Function"),
    @NamedQuery(name = "Suppliers.findByContact2Name", query = "SELECT s FROM Suppliers s WHERE s.contact2Name = :contact2Name"),
    @NamedQuery(name = "Suppliers.findByContact2Email", query = "SELECT s FROM Suppliers s WHERE s.contact2Email = :contact2Email"),
    @NamedQuery(name = "Suppliers.findByContact2Phone", query = "SELECT s FROM Suppliers s WHERE s.contact2Phone = :contact2Phone"),
    @NamedQuery(name = "Suppliers.findByContact2Function", query = "SELECT s FROM Suppliers s WHERE s.contact2Function = :contact2Function"),
    @NamedQuery(name = "Suppliers.findByContact3Name", query = "SELECT s FROM Suppliers s WHERE s.contact3Name = :contact3Name"),
    @NamedQuery(name = "Suppliers.findByContact3Email", query = "SELECT s FROM Suppliers s WHERE s.contact3Email = :contact3Email"),
    @NamedQuery(name = "Suppliers.findByContact3Phone", query = "SELECT s FROM Suppliers s WHERE s.contact3Phone = :contact3Phone"),
    @NamedQuery(name = "Suppliers.findByContact3Function", query = "SELECT s FROM Suppliers s WHERE s.contact3Function = :contact3Function"),
    @NamedQuery(name = "Suppliers.findByContact4Name", query = "SELECT s FROM Suppliers s WHERE s.contact4Name = :contact4Name"),
    @NamedQuery(name = "Suppliers.findByContact4Email", query = "SELECT s FROM Suppliers s WHERE s.contact4Email = :contact4Email"),
    @NamedQuery(name = "Suppliers.findByContact4Phone", query = "SELECT s FROM Suppliers s WHERE s.contact4Phone = :contact4Phone"),
    @NamedQuery(name = "Suppliers.findByContact4Function", query = "SELECT s FROM Suppliers s WHERE s.contact4Function = :contact4Function"),
    @NamedQuery(name = "Suppliers.findByContact5Name", query = "SELECT s FROM Suppliers s WHERE s.contact5Name = :contact5Name"),
    @NamedQuery(name = "Suppliers.findByContact5Email", query = "SELECT s FROM Suppliers s WHERE s.contact5Email = :contact5Email"),
    @NamedQuery(name = "Suppliers.findByContact5Phone", query = "SELECT s FROM Suppliers s WHERE s.contact5Phone = :contact5Phone"),
    @NamedQuery(name = "Suppliers.findByContact5Function", query = "SELECT s FROM Suppliers s WHERE s.contact5Function = :contact5Function"),
    @NamedQuery(name = "Suppliers.findByContact6Name", query = "SELECT s FROM Suppliers s WHERE s.contact6Name = :contact6Name"),
    @NamedQuery(name = "Suppliers.findByContact6Email", query = "SELECT s FROM Suppliers s WHERE s.contact6Email = :contact6Email"),
    @NamedQuery(name = "Suppliers.findByContact6Phone", query = "SELECT s FROM Suppliers s WHERE s.contact6Phone = :contact6Phone"),
    @NamedQuery(name = "Suppliers.findByContact6Function", query = "SELECT s FROM Suppliers s WHERE s.contact6Function = :contact6Function"),
    @NamedQuery(name = "Suppliers.findByBuyer", query = "SELECT s FROM Suppliers s WHERE s.buyer = :buyer"),
    @NamedQuery(name = "Suppliers.findByQm", query = "SELECT s FROM Suppliers s WHERE s.qm = :qm"),
    @NamedQuery(name = "Suppliers.findByBsciPart", query = "SELECT s FROM Suppliers s WHERE s.bsciPart = :bsciPart"),
    @NamedQuery(name = "Suppliers.findByBsciResult", query = "SELECT s FROM Suppliers s WHERE s.bsciResult = :bsciResult"),
    @NamedQuery(name = "Suppliers.findByBsciFrom", query = "SELECT s FROM Suppliers s WHERE s.bsciFrom = :bsciFrom"),
    @NamedQuery(name = "Suppliers.findByBsciTill", query = "SELECT s FROM Suppliers s WHERE s.bsciTill = :bsciTill"),
    @NamedQuery(name = "Suppliers.findByBsciOther1Name", query = "SELECT s FROM Suppliers s WHERE s.bsciOther1Name = :bsciOther1Name"),
    @NamedQuery(name = "Suppliers.findByBsciOther1From", query = "SELECT s FROM Suppliers s WHERE s.bsciOther1From = :bsciOther1From"),
    @NamedQuery(name = "Suppliers.findByBsciOther1Till", query = "SELECT s FROM Suppliers s WHERE s.bsciOther1Till = :bsciOther1Till"),
    @NamedQuery(name = "Suppliers.findByBsciOther2Name", query = "SELECT s FROM Suppliers s WHERE s.bsciOther2Name = :bsciOther2Name"),
    @NamedQuery(name = "Suppliers.findByBsciOther2From", query = "SELECT s FROM Suppliers s WHERE s.bsciOther2From = :bsciOther2From"),
    @NamedQuery(name = "Suppliers.findByBsciOther2Till", query = "SELECT s FROM Suppliers s WHERE s.bsciOther2Till = :bsciOther2Till"),
    @NamedQuery(name = "Suppliers.findByBsciOther3Name", query = "SELECT s FROM Suppliers s WHERE s.bsciOther3Name = :bsciOther3Name"),
    @NamedQuery(name = "Suppliers.findByBsciOther3From", query = "SELECT s FROM Suppliers s WHERE s.bsciOther3From = :bsciOther3From"),
    @NamedQuery(name = "Suppliers.findByBsciOther3Till", query = "SELECT s FROM Suppliers s WHERE s.bsciOther3Till = :bsciOther3Till"),
    @NamedQuery(name = "Suppliers.findByCertIso9000From", query = "SELECT s FROM Suppliers s WHERE s.certIso9000From = :certIso9000From"),
    @NamedQuery(name = "Suppliers.findByCertIso9000", query = "SELECT s FROM Suppliers s WHERE s.certIso9000 = :certIso9000"),
    @NamedQuery(name = "Suppliers.findByCertIso9000Till", query = "SELECT s FROM Suppliers s WHERE s.certIso9000Till = :certIso9000Till"),
    @NamedQuery(name = "Suppliers.findByCertIso9000Name", query = "SELECT s FROM Suppliers s WHERE s.certIso9000Name = :certIso9000Name"),
    @NamedQuery(name = "Suppliers.findByDeclRohs", query = "SELECT s FROM Suppliers s WHERE s.declRohs = :declRohs"),
    @NamedQuery(name = "Suppliers.findByCertIso14000From", query = "SELECT s FROM Suppliers s WHERE s.certIso14000From = :certIso14000From"),
    @NamedQuery(name = "Suppliers.findByCertIso14000", query = "SELECT s FROM Suppliers s WHERE s.certIso14000 = :certIso14000"),
    @NamedQuery(name = "Suppliers.findByCertIso14000Till", query = "SELECT s FROM Suppliers s WHERE s.certIso14000Till = :certIso14000Till"),
    @NamedQuery(name = "Suppliers.findByCertIso14000Name", query = "SELECT s FROM Suppliers s WHERE s.certIso14000Name = :certIso14000Name"),
    @NamedQuery(name = "Suppliers.findBycertOther1", query = "SELECT s FROM Suppliers s WHERE s.certOther1 = :certOther1"),
    @NamedQuery(name = "Suppliers.findByCertOther1From", query = "SELECT s FROM Suppliers s WHERE s.certOther1From = :certOther1From"),
    @NamedQuery(name = "Suppliers.findByCertOther1Till", query = "SELECT s FROM Suppliers s WHERE s.certOther1Till = :certOther1Till"),
    @NamedQuery(name = "Suppliers.findByCertOther1Name", query = "SELECT s FROM Suppliers s WHERE s.certOther1Name = :certOther1Name"),
    @NamedQuery(name = "Suppliers.findBycertOther2", query = "SELECT s FROM Suppliers s WHERE s.certOther2 = :certOther2"),
    @NamedQuery(name = "Suppliers.findByCertOther2From", query = "SELECT s FROM Suppliers s WHERE s.certOther2From = :certOther2From"),
    @NamedQuery(name = "Suppliers.findByCertOther2Till", query = "SELECT s FROM Suppliers s WHERE s.certOther2Till = :certOther2Till"),
    @NamedQuery(name = "Suppliers.findByCertOther2Name", query = "SELECT s FROM Suppliers s WHERE s.certOther2Name = :certOther2Name"),
    @NamedQuery(name = "Suppliers.findByCertOther3", query = "SELECT s FROM Suppliers s WHERE s.certOther3 = :certOther3"),
    @NamedQuery(name = "Suppliers.findByCertOther3From", query = "SELECT s FROM Suppliers s WHERE s.certOther3From = :certOther3From"),
    @NamedQuery(name = "Suppliers.findByCertOther3Till", query = "SELECT s FROM Suppliers s WHERE s.certOther3Till = :certOther3Till"),
    @NamedQuery(name = "Suppliers.findByCertOther3Name", query = "SELECT s FROM Suppliers s WHERE s.certOther3Name = :certOther3Name"),
    @NamedQuery(name = "Suppliers.findByCertOther4", query = "SELECT s FROM Suppliers s WHERE s.certOther4 = :certOther4"),
    @NamedQuery(name = "Suppliers.findByCertOther4From", query = "SELECT s FROM Suppliers s WHERE s.certOther4From = :certOther4From"),
    @NamedQuery(name = "Suppliers.findByCertOther4Till", query = "SELECT s FROM Suppliers s WHERE s.certOther4Till = :certOther4Till"),
    @NamedQuery(name = "Suppliers.findByCertOther4Name", query = "SELECT s FROM Suppliers s WHERE s.certOther4Name = :certOther4Name"),
    @NamedQuery(name = "Suppliers.findByCertOther5", query = "SELECT s FROM Suppliers s WHERE s.certOther5 = :certOther5"),
    @NamedQuery(name = "Suppliers.findByCertOther5From", query = "SELECT s FROM Suppliers s WHERE s.certOther5From = :certOther5From"),
    @NamedQuery(name = "Suppliers.findByCertOther5Till", query = "SELECT s FROM Suppliers s WHERE s.certOther5Till = :certOther5Till"),
    @NamedQuery(name = "Suppliers.findByCertOther5Name", query = "SELECT s FROM Suppliers s WHERE s.certOther5Name = :certOther5Name"),
    @NamedQuery(name = "Suppliers.findByCertOther6", query = "SELECT s FROM Suppliers s WHERE s.certOther6 = :certOther6"),
    @NamedQuery(name = "Suppliers.findByCertOther6From", query = "SELECT s FROM Suppliers s WHERE s.certOther6From = :certOther6From"),
    @NamedQuery(name = "Suppliers.findByCertOther6Till", query = "SELECT s FROM Suppliers s WHERE s.certOther6Till = :certOther6Till"),
    @NamedQuery(name = "Suppliers.findBycertOther6Name", query = "SELECT s FROM Suppliers s WHERE s.certOther6Name = :certOther6Name"),
    @NamedQuery(name = "Suppliers.findByCertOther7", query = "SELECT s FROM Suppliers s WHERE s.certOther7 = :certOther7"),
    @NamedQuery(name = "Suppliers.findByCertOther7From", query = "SELECT s FROM Suppliers s WHERE s.certOther7From = :certOther7From"),
    @NamedQuery(name = "Suppliers.findByCertOther7Till", query = "SELECT s FROM Suppliers s WHERE s.certOther7Till = :certOther7Till"),
    @NamedQuery(name = "Suppliers.findByCertOther7Name", query = "SELECT s FROM Suppliers s WHERE s.certOther7Name = :certOther7Name"),
    @NamedQuery(name = "Suppliers.findByCertOther8", query = "SELECT s FROM Suppliers s WHERE s.certOther8 = :certOther8"),
    @NamedQuery(name = "Suppliers.findByCertOther8From", query = "SELECT s FROM Suppliers s WHERE s.certOther8From = :certOther8From"),
    @NamedQuery(name = "Suppliers.findByCertOther8Till", query = "SELECT s FROM Suppliers s WHERE s.certOther8Till = :certOther8Till"),
    @NamedQuery(name = "Suppliers.findBycertOther8Name", query = "SELECT s FROM Suppliers s WHERE s.certOther8Name = :certOther8Name"),
    @NamedQuery(name = "Suppliers.findByCertOther9", query = "SELECT s FROM Suppliers s WHERE s.certOther9 = :certOther9"),
    @NamedQuery(name = "Suppliers.findByCertOther9From", query = "SELECT s FROM Suppliers s WHERE s.certOther9From = :certOther9From"),
    @NamedQuery(name = "Suppliers.findByCertOther9Till", query = "SELECT s FROM Suppliers s WHERE s.certOther9Till = :certOther9Till"),
    @NamedQuery(name = "Suppliers.findBycertOther9Name", query = "SELECT s FROM Suppliers s WHERE s.certOther9Name = :certOther9Name"),
    @NamedQuery(name = "Suppliers.findByCertOther10", query = "SELECT s FROM Suppliers s WHERE s.certOther10 = :certOther10"),
    @NamedQuery(name = "Suppliers.findByCertOther10From", query = "SELECT s FROM Suppliers s WHERE s.certOther10From = :certOther10From"),
    @NamedQuery(name = "Suppliers.findByCertOther10Till", query = "SELECT s FROM Suppliers s WHERE s.certOther10Till = :certOther10Till"),
    @NamedQuery(name = "Suppliers.findBycertOther10Name", query = "SELECT s FROM Suppliers s WHERE s.certOther10Name = :certOther10Name"),
    @NamedQuery(name = "Suppliers.findByDeclBrandDate", query = "SELECT s FROM Suppliers s WHERE s.declBrandDate = :declBrandDate"),
    @NamedQuery(name = "Suppliers.findByDeclPackDate", query = "SELECT s FROM Suppliers s WHERE s.declPackDate = :declPackDate"),
    @NamedQuery(name = "Suppliers.findByDeclPay", query = "SELECT s FROM Suppliers s WHERE s.declPay = :declPay"),
    @NamedQuery(name = "Suppliers.findByDeclContrDate", query = "SELECT s FROM Suppliers s WHERE s.declContrDate = :declContrDate"),
    @NamedQuery(name = "Suppliers.findByDeclWarranty", query = "SELECT s FROM Suppliers s WHERE s.declWarranty = :declWarranty"),
    @NamedQuery(name = "Suppliers.findByDeclBrand", query = "SELECT s FROM Suppliers s WHERE s.declBrand = :declBrand"),
    @NamedQuery(name = "Suppliers.findByDeclPack", query = "SELECT s FROM Suppliers s WHERE s.declPack = :declPack"),
    @NamedQuery(name = "Suppliers.findByDeclContr", query = "SELECT s FROM Suppliers s WHERE s.declContr = :declContr"),
    @NamedQuery(name = "Suppliers.findByDeclReach", query = "SELECT s FROM Suppliers s WHERE s.declReach = :declReach"),
    @NamedQuery(name = "Suppliers.findByDeclReachDate", query = "SELECT s FROM Suppliers s WHERE s.declReachDate = :declReachDate"),
    @NamedQuery(name = "Suppliers.findByDeclRohsDate", query = "SELECT s FROM Suppliers s WHERE s.declRohsDate = :declRohsDate"),
    @NamedQuery(name = "Suppliers.findByDeclSda", query = "SELECT s FROM Suppliers s WHERE s.declSda = :declSda"),
    @NamedQuery(name = "Suppliers.findByDeclSdaDate", query = "SELECT s FROM Suppliers s WHERE s.declSdaDate = :declSdaDate"),
    @NamedQuery(name = "Suppliers.findByDeclSop", query = "SELECT s FROM Suppliers s WHERE s.declSop = :declSop"),
    @NamedQuery(name = "Suppliers.findByDeclSopDate", query = "SELECT s FROM Suppliers s WHERE s.declSopDate = :declSopDate"),
    @NamedQuery(name = "Suppliers.findByFolder", query = "SELECT s FROM Suppliers s WHERE s.folder = :folder")})
public class Suppliers implements Serializable {

    @Transient
    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    @Column(name = "SUPPLIER")
    private String supplier;
    @Column(name = "OFFICE_VENDOR")
    private int officeVendor;
    @Column(name = "OFFICE_VENDOR_SFE")
    private int officeVendorSFE;
    @Column(name = "OFFICE_NAME")
    private String officeName;
    @Column(name = "OFFICE_NAME1")
    private String officeName1;
    @Column(name = "OFFICE_NAME2")
    private String officeName2;
    @Column(name = "OFFICE_ADDRESS1")
    private String officeAddress1;
    @Column(name = "OFFICE_ADDRESS2")
    private String officeAddress2;
    @Column(name = "OFFICE_ADDRESS3")
    private String officeAddress3;
    @Column(name = "OFFICE_CITY")
    private String officeCity;
    @Column(name = "OFFICE_PROVINCE")
    private String officeProvince;
    @Column(name = "OFFICE_COUNTRY")
    private String officeCountry;
    @Column(name = "OFFICE_ZIP")
    private String officeZip;
    @Column(name = "OFFICE_WWW")
    private String officeWww;
    @Column(name = "DB_ID")
    private String dbID;
    @Column(name = "FACTORY1_VENDOR")
    private int factory1Vendor;
    @Column(name = "FACTORY1_NAME")
    private String factory1Name;
    @Column(name = "FACTORY1_NAME1")
    private String factory1Name1;
    @Column(name = "FACTORY1_NAME2")
    private String factory1Name2;
    @Column(name = "FACTORY1_ADDRESS1")
    private String factory1Address1;
    @Column(name = "FACTORY1_ADDRESS2")
    private String factory1Address2;
    @Column(name = "FACTORY1_ADDRESS3")
    private String factory1Address3;
    @Column(name = "FACTORY1_CITY")
    private String factory1City;
    @Column(name = "FACTORY1_PROVINCE")
    private String factory1Province;
    @Column(name = "FACTORY1_COUNTRY")
    private String factory1Country;
    @Column(name = "FACTORY1_ZIP")
    private String factory1Zip;
    @Column(name = "FACTORY1_WWW")
    private String factory1Www;
    @Column(name = "DB_ID1")
    private String dbID1;
    @Column(name = "FACTORY2_VENDOR")
    private int factory2Vendor;
    @Column(name = "FACTORY2_NAME")
    private String factory2Name;
    @Column(name = "FACTORY2_NAME1")
    private String factory2Name1;
    @Column(name = "FACTORY2_NAME2")
    private String factory2Name2;
    @Column(name = "FACTORY2_ADDRESS1")
    private String factory2Address1;
    @Column(name = "FACTORY2_ADDRESS2")
    private String factory2Address2;
    @Column(name = "FACTORY2_ADDRESS3")
    private String factory2Address3;
    @Column(name = "FACTORY2_CITY")
    private String factory2City;
    @Column(name = "FACTORY2_PROVINCE")
    private String factory2Province;
    @Column(name = "FACTORY2_COUNTRY")
    private String factory2Country;
    @Column(name = "FACTORY2_ZIP")
    private String factory2Zip;
    @Column(name = "FACTORY2_WWW")
    private String factory2Www;
    @Column(name = "DB_ID2")
    private String dbID2;
    @Column(name = "FACTORY3_VENDOR")
    private int factory3Vendor;
    @Column(name = "FACTORY3_NAME")
    private String factory3Name;
    @Column(name = "FACTORY3_NAME1")
    private String factory3Name1;
    @Column(name = "FACTORY3_NAME2")
    private String factory3Name2;
    @Column(name = "FACTORY3_ADDRESS1")
    private String factory3Address1;
    @Column(name = "FACTORY3_ADDRESS2")
    private String factory3Address2;
    @Column(name = "FACTORY3_ADDRESS3")
    private String factory3Address3;
    @Column(name = "FACTORY3_CITY")
    private String factory3City;
    @Column(name = "FACTORY3_PROVINCE")
    private String factory3Province;
    @Column(name = "FACTORY3_COUNTRY")
    private String factory3Country;
    @Column(name = "FACTORY3_ZIP")
    private String factory3Zip;
    @Column(name = "FACTORY3_WWW")
    private String factory3Www;
    @Column(name = "DB_ID3")
    private String dbID3;
    @Basic(optional = false)
    @Column(name = "FOB")
    private boolean FOB;
    @Column(name = "NOTE")
    private String note;
    @Column(name = "CONTACT1_NAME")
    private String contact1Name;
    @Column(name = "CONTACT1_EMAIL")
    private String contact1Email;
    @Column(name = "CONTACT1_PHONE")
    private String contact1Phone;
    @Column(name = "CONTACT1_FUNCTION")
    private String contact1Function;
    @Column(name = "CONTACT2_NAME")
    private String contact2Name;
    @Column(name = "CONTACT2_EMAIL")
    private String contact2Email;
    @Column(name = "CONTACT2_PHONE")
    private String contact2Phone;
    @Column(name = "CONTACT2_FUNCTION")
    private String contact2Function;
    @Column(name = "CONTACT3_NAME")
    private String contact3Name;
    @Column(name = "CONTACT3_EMAIL")
    private String contact3Email;
    @Column(name = "CONTACT3_PHONE")
    private String contact3Phone;
    @Column(name = "CONTACT3_FUNCTION")
    private String contact3Function;
    @Column(name = "CONTACT4_NAME")
    private String contact4Name;
    @Column(name = "CONTACT4_EMAIL")
    private String contact4Email;
    @Column(name = "CONTACT4_PHONE")
    private String contact4Phone;
    @Column(name = "CONTACT4_FUNCTION")
    private String contact4Function;
    @Column(name = "CONTACT5_NAME")
    private String contact5Name;
    @Column(name = "CONTACT5_EMAIL")
    private String contact5Email;
    @Column(name = "CONTACT5_PHONE")
    private String contact5Phone;
    @Column(name = "CONTACT5_FUNCTION")
    private String contact5Function;
    @Column(name = "CONTACT6_NAME")
    private String contact6Name;
    @Column(name = "CONTACT6_EMAIL")
    private String contact6Email;
    @Column(name = "CONTACT6_PHONE")
    private String contact6Phone;
    @Column(name = "CONTACT6_FUNCTION")
    private String contact6Function;
    @Column(name = "BUYER")
    private String buyer;
    @Column(name = "QM")
    private String qm;
    @Column(name = "BSCI_PART")
    private String bsciPart;
    @Column(name = "BSCI_RESULT")
    private String bsciResult;
    @Column(name = "BSCI_FROM")
    @Temporal(TemporalType.DATE)
    private Date bsciFrom;
    @Column(name = "BSCI_TILL")
    @Temporal(TemporalType.DATE)
    private Date bsciTill;
    @Column(name = "BSCI_OTHER1_NAME")
    private String bsciOther1Name;
    @Column(name = "BSCI_OTHER1_FROM")
    @Temporal(TemporalType.DATE)
    private Date bsciOther1From;
    @Column(name = "BSCI_OTHER1_TILL")
    @Temporal(TemporalType.DATE)
    private Date bsciOther1Till;
    @Column(name = "BSCI_OTHER2_NAME")
    private String bsciOther2Name;
    @Column(name = "BSCI_OTHER2_FROM")
    @Temporal(TemporalType.DATE)
    private Date bsciOther2From;
    @Column(name = "BSCI_OTHER2_TILL")
    @Temporal(TemporalType.DATE)
    private Date bsciOther2Till;
    @Column(name = "BSCI_OTHER3_NAME")
    private String bsciOther3Name;
    @Column(name = "BSCI_OTHER3_FROM")
    @Temporal(TemporalType.DATE)
    private Date bsciOther3From;
    @Column(name = "BSCI_OTHER3_TILL")
    @Temporal(TemporalType.DATE)
    private Date bsciOther3Till;
    @Column(name = "CERT_ISO9000")
    private String certIso9000;
    @Column(name = "CERT_ISO9000_FROM")
    @Temporal(TemporalType.DATE)
    private Date certIso9000From;
    @Column(name = "CERT_ISO9000_TILL")
    @Temporal(TemporalType.DATE)
    private Date certIso9000Till;
    @Column(name = "CERT_ISO9000_NAME")
    private String certIso9000Name;
    @Basic(optional = false)
    @Column(name = "DECL_ROHS")
    private boolean declRohs;
    @Column(name = "CERT_ISO14000")
    private String certIso14000;
    @Column(name = "CERT_ISO14000_FROM")
    @Temporal(TemporalType.DATE)
    private Date certIso14000From;
    @Column(name = "CERT_ISO14000_TILL")
    @Temporal(TemporalType.DATE)
    private Date certIso14000Till;
    @Column(name = "CERT_ISO14000_NAME")
    private String certIso14000Name;
    @Basic(optional = false)
    @Column(name = "DECL_REACH")
    private boolean declReach;
    @Column(name = "CERT_OTHER1")
    private String certOther1;
    @Column(name = "CERT_OTHER1_FROM")
    @Temporal(TemporalType.DATE)
    private Date certOther1From;
    @Column(name = "CERT_OTHER1_TILL")
    @Temporal(TemporalType.DATE)
    private Date certOther1Till;
    @Column(name = "CERT_OTHER1_NAME")
    private String certOther1Name;
    @Column(name = "CERT_OTHER2")
    private String certOther2;
    @Column(name = "CERT_OTHER2_FROM")
    @Temporal(TemporalType.DATE)
    private Date certOther2From;
    @Column(name = "CERT_OTHER2_TILL")
    @Temporal(TemporalType.DATE)
    private Date certOther2Till;
    @Column(name = "CERT_OTHER2_NAME")
    private String certOther2Name;
    @Column(name = "DECL_REACH_DATE")
    @Temporal(TemporalType.DATE)
    private Date declReachDate;
    @Column(name = "CERT_OTHER3")
    private String certOther3;
    @Column(name = "CERT_OTHER3_FROM")
    @Temporal(TemporalType.DATE)
    private Date certOther3From;
    @Column(name = "CERT_OTHER3_TILL")
    @Temporal(TemporalType.DATE)
    private Date certOther3Till;
    @Column(name = "CERT_OTHER3_NAME")
    private String certOther3Name;
    @Column(name = "CERT_OTHER4")
    private String certOther4;
    @Column(name = "CERT_OTHER4_FROM")
    @Temporal(TemporalType.DATE)
    private Date certOther4From;
    @Column(name = "CERT_OTHER4_TILL")
    @Temporal(TemporalType.DATE)
    private Date certOther4Till;
    @Column(name = "CERT_OTHER4_NAME")
    private String certOther4Name;
    @Column(name = "CERT_OTHER5")
    private String certOther5;
    @Column(name = "CERT_OTHER5_FROM")
    @Temporal(TemporalType.DATE)
    private Date certOther5From;
    @Column(name = "CERT_OTHER5_TILL")
    @Temporal(TemporalType.DATE)
    private Date certOther5Till;
    @Column(name = "CERT_OTHER5_NAME")
    private String certOther5Name;
    @Column(name = "CERT_OTHER6")
    private String certOther6;
    @Column(name = "CERT_OTHER6_FROM")
    @Temporal(TemporalType.DATE)
    private Date certOther6From;
    @Column(name = "CERT_OTHER6_TILL")
    @Temporal(TemporalType.DATE)
    private Date certOther6Till;
    @Column(name = "CERT_OTHER6_NAME")
    private String certOther6Name;
    @Column(name = "CERT_OTHER7")
    private String certOther7;
    @Column(name = "CERT_OTHER7_FROM")
    @Temporal(TemporalType.DATE)
    private Date certOther7From;
    @Column(name = "CERT_OTHER7_TILL")
    @Temporal(TemporalType.DATE)
    private Date certOther7Till;
    @Column(name = "CERT_OTHER7_NAME")
    private String certOther7Name;
    @Column(name = "CERT_OTHER8")
    private String certOther8;
    @Column(name = "CERT_OTHER8_FROM")
    @Temporal(TemporalType.DATE)
    private Date certOther8From;
    @Column(name = "CERT_OTHER8_TILL")
    @Temporal(TemporalType.DATE)
    private Date certOther8Till;
    @Column(name = "CERT_OTHER8_NAME")
    private String certOther8Name;
    @Column(name = "CERT_OTHER9")
    private String certOther9;
    @Column(name = "CERT_OTHER9_FROM")
    @Temporal(TemporalType.DATE)
    private Date certOther9From;
    @Column(name = "CERT_OTHER9_TILL")
    @Temporal(TemporalType.DATE)
    private Date certOther9Till;
    @Column(name = "CERT_OTHER9_NAME")
    private String certOther9Name;
    @Column(name = "CERT_OTHER10")
    private String certOther10;
    @Column(name = "CERT_OTHER10_FROM")
    @Temporal(TemporalType.DATE)
    private Date certOther10From;
    @Column(name = "CERT_OTHER10_TILL")
    @Temporal(TemporalType.DATE)
    private Date certOther10Till;
    @Column(name = "CERT_OTHER10_NAME")
    private String certOther10Name;
    @Column(name = "DECL_ROHS_DATE")
    @Temporal(TemporalType.DATE)
    private Date declRohsDate;
    @Column(name = "DECL_BRAND_DATE")
    @Temporal(TemporalType.DATE)
    private Date declBrandDate;
    @Column(name = "DECL_PACK_DATE")
    @Temporal(TemporalType.DATE)
    private Date declPackDate;
    @Column(name = "DECL_CONTR_DATE")
    @Temporal(TemporalType.DATE)
    private Date declContrDate;
    @Column(name = "DECL_PAY")
    private String declPay;
    @Column(name = "DECL_WARRANTY")
    private String declWarranty;
    @Basic(optional = false)
    @Column(name = "DECL_BRAND")
    private boolean declBrand;
    @Basic(optional = false)
    @Column(name = "DECL_PACK")
    private boolean declPack;
    @Basic(optional = false)
    @Column(name = "DECL_CONTR")
    private boolean declContr;
    @Column(name = "DECL_SDA")
    private boolean declSda;
    @Column(name = "DECL_SDA_DATE")
    @Temporal(TemporalType.DATE)
    private Date declSdaDate;
    @Column(name = "DECL_SOP")
    private boolean declSop;
    @Column(name = "DECL_SOP_DATE")
    @Temporal(TemporalType.DATE)
    private Date declSopDate;
    @Column(name = "FOLDER")
    private String folder;

    public Suppliers() {
    }

    public Suppliers(String supplier) {
        this.supplier = supplier;
    }

    public Suppliers(String supplier, boolean FOB, boolean declBrand, boolean declPack,
            boolean declContr, boolean declReach, boolean declRohs, boolean declSda, boolean declSop) {
        this.supplier = supplier;
        this.FOB = FOB;
        this.declBrand = declBrand;
        this.declPack = declPack;
        this.declContr = declContr;
        this.declReach = declReach;
        this.declRohs = declRohs;
        this.declSda = declSda;
        this.declSop = declSop;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        String oldSupplier = this.supplier;
        this.supplier = supplier;
        changeSupport.firePropertyChange("supplier", oldSupplier, supplier);
    }

    public int getOfficeVendor() {
        return officeVendor;
    }

    public void setOfficeVendor(int officeVendor) {
        int oldofficeVendor = this.officeVendor;
        this.officeVendor = officeVendor;
        changeSupport.firePropertyChange("officeVendor", oldofficeVendor, officeVendor);
    }

    public int getOfficeVendorSFE() {
        return officeVendorSFE;
    }

    public void setOfficeVendorSFE(int officeVendorSFE) {
        int oldofficeVendorSFE = this.officeVendorSFE;
        this.officeVendorSFE = officeVendorSFE;
        changeSupport.firePropertyChange("officeVendorSFE", oldofficeVendorSFE, officeVendorSFE);
    }
    
    public String getOfficeName() {
        return officeName;
    }

    public void setOfficeName(String officeName) {
        String oldOfficeName = this.officeName;
        this.officeName = officeName;
        changeSupport.firePropertyChange("officeName", oldOfficeName, officeName);
    }

    public String getOfficeName1() {
        return officeName1;
    }

    public void setOfficeName1(String officeName1) {
        String oldOfficeName1 = this.officeName1;
        this.officeName1 = officeName1;
        changeSupport.firePropertyChange("officeName1", oldOfficeName1, officeName1);
    }
    
    public String getOfficeName2() {
        return officeName2;
    }

    public void setOfficeName2(String officeName2) {
        String oldOfficeName2 = this.officeName2;
        this.officeName2 = officeName2;
        changeSupport.firePropertyChange("officeName2", oldOfficeName2, officeName2);
    }
    
    public String getOfficeAddress1() {
        return officeAddress1;
    }

    public void setOfficeAddress1(String officeAddress1) {
        String oldOfficeAddress1 = this.officeAddress1;
        this.officeAddress1 = officeAddress1;
        changeSupport.firePropertyChange("officeAddress1", oldOfficeAddress1, officeAddress1);
    }
    
    public String getOfficeAddress2() {
        return officeAddress2;
    }

    public void setOfficeAddress2(String officeAddress2) {
        String oldOfficeAddress2 = this.officeAddress2;
        this.officeAddress2 = officeAddress2;
        changeSupport.firePropertyChange("officeAddress2", oldOfficeAddress2, officeAddress2);
    }
    
    public String getOfficeAddress3() {
        return officeAddress3;
    }

    public void setOfficeAddress3(String officeAddress3) {
        String oldOfficeAddress3 = this.officeAddress3;
        this.officeAddress3 = officeAddress3;
        changeSupport.firePropertyChange("officeAddress3", oldOfficeAddress3, officeAddress3);
    }
    
    public String getOfficeCity() {
        return officeCity;
    }

    public void setOfficeCity(String officeCity) {
        String oldOfficeCity = this.officeCity;
        this.officeCity = officeCity;
        changeSupport.firePropertyChange("officeCity", oldOfficeCity, officeCity);
    }
    
    public String getOfficeProvince() {
        return officeProvince;
    }

    public void setOfficeProvince(String officeProvince) {
        String oldOfficeProvince = this.officeProvince;
        this.officeProvince = officeProvince;
        changeSupport.firePropertyChange("officeProvince", oldOfficeProvince, officeProvince);
    }
    
    public String getOfficeCountry() {
        return officeCountry;
    }

    public void setOfficeCountry(String officeCountry) {
        String oldOfficeCountry = this.officeCountry;
        this.officeCountry = officeCountry;
        changeSupport.firePropertyChange("officeCountry", oldOfficeCountry, officeCountry);
    }
    
    public String getOfficeZip() {
        return officeZip;
    }

    public void setOfficeZip(String officeZip) {
        String oldOfficeZip = this.officeZip;
        this.officeZip = officeZip;
        changeSupport.firePropertyChange("officeZip", oldOfficeZip, officeZip);
    }
    
    public String getOfficeWww() {
        return officeWww;
    }

    public void setOfficeWww(String officeWww) {
        String oldOfficeWww = this.officeWww;
        this.officeWww = officeWww;
        changeSupport.firePropertyChange("officeWww", oldOfficeWww, officeWww);
    }
    
    public String getDBID() {
        return dbID;
    }

    public void setDBID(String dbID) {
        String olddbID = this.dbID;
        this.dbID = dbID;
        changeSupport.firePropertyChange("dbID", olddbID, dbID);
    }

    public int getFactory1Vendor() {
        return factory1Vendor;
    }

    public void setFactory1Vendor(int factory1Vendor) {
        int oldfactory1Vendor = this.factory1Vendor;
        this.factory1Vendor = factory1Vendor;
        changeSupport.firePropertyChange("factory1Vendor", oldfactory1Vendor, factory1Vendor);
    }
    
    public String getFactory1Name() {
        return factory1Name;
    }

    public void setFactory1Name(String factory1Name) {
        String oldFactory1Name = this.factory1Name;
        this.factory1Name = factory1Name;
        changeSupport.firePropertyChange("factory1Name", oldFactory1Name, factory1Name);
    }
    
    public String getFactory1Name1() {
        return factory1Name1;
    }

    public void setFactory1Name1(String factory1Name1) {
        String oldFactory1Name1 = this.factory1Name1;
        this.factory1Name1 = factory1Name1;
        changeSupport.firePropertyChange("factory1Name1", oldFactory1Name1, factory1Name1);
    }
    
    public String getFactory1Name2() {
        return factory1Name2;
    }

    public void setFactory1Name2(String factory1Name2) {
        String oldFactory1Name2 = this.factory1Name2;
        this.factory1Name2 = factory1Name2;
        changeSupport.firePropertyChange("factory1Name2", oldFactory1Name2, factory1Name2);
    }
    
    public String getFactory1Address1() {
        return factory1Address1;
    }

    public void setFactory1Address1(String factory1Address1) {
        String oldFactory1Address1 = this.factory1Address1;
        this.factory1Address1 = factory1Address1;
        changeSupport.firePropertyChange("factory1Address1", oldFactory1Address1, factory1Address1);
    }
    
    public String getFactory1Address2() {
        return factory1Address2;
    }

    public void setFactory1Address2(String factory1Address2) {
        String oldFactory1Address2 = this.factory1Address2;
        this.factory1Address2 = factory1Address2;
        changeSupport.firePropertyChange("factory1Address2", oldFactory1Address2, factory1Address2);
    }
    
    public String getFactory1Address3() {
        return factory1Address3;
    }

    public void setFactory1Address3(String factory1Address3) {
        String oldFactory1Address3 = this.factory1Address3;
        this.factory1Address3 = factory1Address3;
        changeSupport.firePropertyChange("factory1Address3", oldFactory1Address3, factory1Address3);
    }
    
    public String getFactory1City() {
        return factory1City;
    }

    public void setFactory1City(String factory1City) {
        String oldFactory1City = this.factory1City;
        this.factory1City = factory1City;
        changeSupport.firePropertyChange("factory1City", oldFactory1City, factory1City);
    }
    
    public String getFactory1Province() {
        return factory1Province;
    }

    public void setFactory1Province(String factory1Province) {
        String oldFactory1Province = this.factory1Province;
        this.factory1Province = factory1Province;
        changeSupport.firePropertyChange("factory1Province", oldFactory1Province, factory1Province);
    }
    
    public String getFactory1Country() {
        return factory1Country;
    }

    public void setFactory1Country(String factory1Country) {
        String oldFactory1Country = this.factory1Country;
        this.factory1Country = factory1Country;
        changeSupport.firePropertyChange("factory1Country", oldFactory1Country, factory1Country);
    }
    
    public String getFactory1Zip() {
        return factory1Zip;
    }

    public void setFactory1Zip(String factory1Zip) {
        String oldFactory1Zip = this.factory1Zip;
        this.factory1Zip = factory1Zip;
        changeSupport.firePropertyChange("factory1Zip", oldFactory1Zip, factory1Zip);
    }
    
    public String getFactory1Www() {
        return factory1Www;
    }

    public void setFactory1Www(String factory1Www) {
        String oldFactory1Www = this.factory1Www;
        this.factory1Www = factory1Www;
        changeSupport.firePropertyChange("factory1Www", oldFactory1Www, factory1Www);
    }
    
    public String getDBID1() {
        return dbID1;
    }
    
    public void setDBID1(String dbID1) {
        String olddbID1 = this.dbID1;
        this.dbID1 = dbID1;
        changeSupport.firePropertyChange("dbID1", olddbID1, dbID1);
    }
    
    public int getFactory2Vendor() {
        return factory2Vendor;
    }

    public void setFactory2Vendor(int factory2Vendor) {
        int oldfactory2Vendor = this.factory2Vendor;
        this.factory2Vendor = factory2Vendor;
        changeSupport.firePropertyChange("factory2Vendor", oldfactory2Vendor, factory2Vendor);
    }
    
    public String getFactory2Name() {
        return factory2Name;
    }

    public void setFactory2Name(String factory2Name) {
        String oldFactory2Name = this.factory2Name;
        this.factory2Name = factory2Name;
        changeSupport.firePropertyChange("factory2Name", oldFactory2Name, factory2Name);
    }
    
    public String getFactory2Name1() {
        return factory2Name1;
    }

    public void setFactory2Name1(String factory2Name1) {
        String oldFactory2Name1 = this.factory2Name1;
        this.factory2Name1 = factory2Name1;
        changeSupport.firePropertyChange("factory2Name1", oldFactory2Name1, factory2Name1);
    }
    
    public String getFactory2Name2() {
        return factory2Name2;
    }

    public void setFactory2Name2(String factory2Name2) {
        String oldFactory2Name2 = this.factory2Name2;
        this.factory2Name2 = factory2Name2;
        changeSupport.firePropertyChange("factory2Name2", oldFactory2Name2, factory2Name2);
    }
    
    public String getFactory2Address1() {
        return factory2Address1;
    }

    public void setFactory2Address1(String factory2Address1) {
        String oldFactory2Address1 = this.factory2Address1;
        this.factory2Address1 = factory2Address1;
        changeSupport.firePropertyChange("factory2Address1", oldFactory2Address1, factory2Address1);
    }
    
    public String getFactory2Address2() {
        return factory2Address2;
    }

    public void setFactory2Address2(String factory2Address2) {
        String oldFactory2Address2 = this.factory2Address2;
        this.factory2Address2 = factory2Address2;
        changeSupport.firePropertyChange("factory2Address2", oldFactory2Address2, factory2Address2);
    }
    
    public String getFactory2Address3() {
        return factory2Address3;
    }

    public void setFactory2Address3(String factory2Address3) {
        String oldFactory2Address3 = this.factory2Address3;
        this.factory2Address3 = factory2Address3;
        changeSupport.firePropertyChange("factory2Address3", oldFactory2Address3, factory2Address3);
    }
    
    public String getFactory2City() {
        return factory2City;
    }

    public void setFactory2City(String factory2City) {
        String oldFactory2City = this.factory2City;
        this.factory2City = factory2City;
        changeSupport.firePropertyChange("factory2City", oldFactory2City, factory2City);
    }
    
    public String getFactory2Province() {
        return factory2Province;
    }

    public void setFactory2Province(String factory2Province) {
        String oldFactory2Province = this.factory2Province;
        this.factory2Province = factory2Province;
        changeSupport.firePropertyChange("factory2Province", oldFactory2Province, factory2Province);
    }
    
    public String getFactory2Country() {
        return factory2Country;
    }

    public void setFactory2Country(String factory2Country) {
        String oldFactory2Country = this.factory2Country;
        this.factory2Country = factory2Country;
        changeSupport.firePropertyChange("factory2Country", oldFactory2Country, factory2Country);
    }
    
    public String getFactory2Zip() {
        return factory2Zip;
    }

    public void setFactory2Zip(String factory2Zip) {
        String oldFactory2Zip = this.factory2Zip;
        this.factory2Zip = factory2Zip;
        changeSupport.firePropertyChange("factory2Zip", oldFactory2Zip, factory2Zip);
    }
    
    public String getFactory2Www() {
        return factory2Www;
    }

    public void setFactory2Www(String factory2Www) {
        String oldFactory2Www = this.factory2Www;
        this.factory2Www = factory2Www;
        changeSupport.firePropertyChange("factory2Www", oldFactory2Www, factory2Www);
    }
    
    public String getDBID2() {
        return dbID2;
    }

    public void setDBID2(String dbID2) {
        String olddbID2 = this.dbID2;
        this.dbID2 = dbID2;
        changeSupport.firePropertyChange("dbID2", olddbID2, dbID2);
    }
    
    public int getFactory3Vendor() {
        return factory3Vendor;
    }

    public void setFactory3Vendor(int factory3Vendor) {
        int oldfactory3Vendor = this.factory3Vendor;
        this.factory3Vendor = factory3Vendor;
        changeSupport.firePropertyChange("factory3Vendor", oldfactory3Vendor, factory3Vendor);
    }
    
    public String getFactory3Name() {
        return factory3Name;
    }
   
    public void setFactory3Name(String factory3Name) {
        String oldFactory3Name = this.factory3Name;
        this.factory3Name = factory3Name;
        changeSupport.firePropertyChange("factory3Name", oldFactory3Name, factory3Name);
    }

    public String getFactory3Name1() {
        return factory3Name1;
    }

    public void setFactory3Name1(String factory3Name1) {
        String oldFactory3Name1 = this.factory3Name1;
        this.factory3Name1 = factory3Name1;
        changeSupport.firePropertyChange("factory3Name1", oldFactory3Name1, factory3Name1);
    }
    
    public String getFactory3Name2() {
        return factory3Name2;
    }

    public void setFactory3Name2(String factory3Name2) {
        String oldFactory3Name2 = this.factory3Name2;
        this.factory3Name2 = factory3Name2;
        changeSupport.firePropertyChange("factory3Name2", oldFactory3Name2, factory3Name2);
    }
    
    public String getFactory3Address1() {
        return factory3Address1;
    }

    public void setFactory3Address1(String factory3Address1) {
        String oldFactory3Address1 = this.factory3Address1;
        this.factory3Address1 = factory3Address1;
        changeSupport.firePropertyChange("factory3Address1", oldFactory3Address1, factory3Address1);
    }
    
    public String getFactory3Address2() {
        return factory3Address2;
    }

    public void setFactory3Address2(String factory3Address2) {
        String oldFactory3Address2 = this.factory3Address2;
        this.factory3Address2 = factory3Address2;
        changeSupport.firePropertyChange("factory3Address2", oldFactory3Address2, factory3Address2);
    }
    
    public String getFactory3Address3() {
        return factory3Address3;
    }

    public void setFactory3Address3(String factory3Address3) {
        String oldFactory3Address3 = this.factory3Address3;
        this.factory3Address3 = factory3Address3;
        changeSupport.firePropertyChange("factory3Address3", oldFactory3Address3, factory3Address3);
    }
    
    public String getFactory3City() {
        return factory3City;
    }

    public void setFactory3City(String factory3City) {
        String oldFactory3City = this.factory3City;
        this.factory3City = factory3City;
        changeSupport.firePropertyChange("factory3City", oldFactory3City, factory3City);
    }
    
    public String getFactory3Province() {
        return factory3Province;
    }

    public void setFactory3Province(String factory3Province) {
        String oldFactory3Province = this.factory3Province;
        this.factory3Province = factory3Province;
        changeSupport.firePropertyChange("factory3Province", oldFactory3Province, factory3Province);
    }
    
    public String getFactory3Country() {
        return factory3Country;
    }

    public void setFactory3Country(String factory3Country) {
        String oldFactory3Country = this.factory3Country;
        this.factory3Country = factory3Country;
        changeSupport.firePropertyChange("factory3Country", oldFactory3Country, factory3Country);
    }
    
    public String getFactory3Zip() {
        return factory3Zip;
    }

    public void setFactory3Zip(String factory3Zip) {
        String oldFactory3Zip = this.factory3Zip;
        this.factory3Zip = factory3Zip;
        changeSupport.firePropertyChange("factory3Zip", oldFactory3Zip, factory3Zip);
    }
    
    public String getFactory3Www() {
        return factory3Www;
    }

    public void setFactory3Www(String factory3Www) {
        String oldFactory3Www = this.factory3Www;
        this.factory3Www = factory3Www;
        changeSupport.firePropertyChange("factory3Www", oldFactory3Www, factory3Www);
    }
    
    public String getDBID3() {
        return dbID3;
    }

    public void setDBID3(String dbID3) {
        String olddbID3 = this.dbID3;
        this.dbID3 = dbID3;
        changeSupport.firePropertyChange("dbID3", olddbID3, dbID3);
    }
    
    public boolean getFOB() {
        return FOB;
    }

    public void setFOB(boolean FOB) {
        boolean oldFOB = this.FOB;
        this.FOB = FOB;
        changeSupport.firePropertyChange("FOB", oldFOB, FOB);
    }
    
    public String getNote() {
        return note;
    }
    
    public void setNote(String note) {
        String oldNote = this.note;
        this.note = note;
        changeSupport.firePropertyChange("note", oldNote, note);
    }

    public String getContact1Name() {
        return contact1Name;
    }

    public void setContact1Name(String contact1Name) {
        String oldContact1Name = this.contact1Name;
        this.contact1Name = contact1Name;
        changeSupport.firePropertyChange("contact1Name", oldContact1Name, contact1Name);
    }
    
    public String getContact1Email() {
        return contact1Email;
    }

    public void setContact1Email(String contact1Email) {
        String oldContact1Email = this.contact1Email;
        this.contact1Email = contact1Email;
        changeSupport.firePropertyChange("contact1Email", oldContact1Email, contact1Email);
    }
    
    public String getContact1Phone() {
        return contact1Phone;
    }

    public void setContact1Phone(String contact1Phone) {
        String oldContact1Phone = this.contact1Phone;
        this.contact1Phone = contact1Phone;
        changeSupport.firePropertyChange("contact1Phone", oldContact1Phone, contact1Phone);
    }
    
    public String getContact1Function() {
        return contact1Function;
    }

    public void setContact1Function(String contact1Function) {
        String oldContact1Function = this.contact1Function;
        this.contact1Function = contact1Function;
        changeSupport.firePropertyChange("contact1Function", oldContact1Function, contact1Function);
    }
    
    public String getContact2Name() {
        return contact2Name;
    }

    public void setContact2Name(String contact2Name) {
        String oldContact2Name = this.contact2Name;
        this.contact2Name = contact2Name;
        changeSupport.firePropertyChange("contact2Name", oldContact2Name, contact2Name);
    }
    
    public String getContact2Email() {
        return contact2Email;
    }

    public void setContact2Email(String contact2Email) {
        String oldContact2Email = this.contact2Email;
        this.contact2Email = contact2Email;
        changeSupport.firePropertyChange("contact2Email", oldContact2Email, contact2Email);
    }
    
    public String getContact2Phone() {
        return contact2Phone;
    }

    public void setContact2Phone(String contact2Phone) {
        String oldContact2Phone = this.contact2Phone;
        this.contact2Phone = contact2Phone;
        changeSupport.firePropertyChange("contact2Phone", oldContact2Phone, contact2Phone);
    }
    
    public String getContact2Function() {
        return contact2Function;
    }

    public void setContact2Function(String contact2Function) {
        String oldContact2Function = this.contact2Function;
        this.contact2Function = contact2Function;
        changeSupport.firePropertyChange("contact2Function", oldContact2Function, contact2Function);
    }
    
    public String getContact3Name() {
        return contact3Name;
    }

    public void setContact3Name(String contact3Name) {
        String oldContact3Name = this.contact3Name;
        this.contact3Name = contact3Name;
        changeSupport.firePropertyChange("contact3Name", oldContact3Name, contact3Name);
    }
    
    public String getContact3Email() {
        return contact3Email;
    }

    public void setContact3Email(String contact3Email) {
        String oldContact3Email = this.contact3Email;
        this.contact3Email = contact3Email;
        changeSupport.firePropertyChange("contact3Email", oldContact3Email, contact3Email);
    }
    
    public String getContact3Phone() {
        return contact3Phone;
    }

    public void setContact3Phone(String contact3Phone) {
        String oldContact3Phone = this.contact3Phone;
        this.contact3Phone = contact3Phone;
        changeSupport.firePropertyChange("contact3Phone", oldContact3Phone, contact3Phone);
    }
    
    public String getContact3Function() {
        return contact3Function;
    }

    public void setContact3Function(String contact3Function) {
        String oldContact3Function = this.contact3Function;
        this.contact3Function = contact3Function;
        changeSupport.firePropertyChange("contact3Function", oldContact3Function, contact3Function);
    }
    
    public String getContact4Name() {
        return contact4Name;
    }

    public void setContact4Name(String contact4Name) {
        String oldContact4Name = this.contact4Name;
        this.contact4Name = contact4Name;
        changeSupport.firePropertyChange("contact4Name", oldContact4Name, contact4Name);
    }
    
    public String getContact4Email() {
        return contact4Email;
    }

    public void setContact4Email(String contact4Email) {
        String oldContact4Email = this.contact4Email;
        this.contact4Email = contact4Email;
        changeSupport.firePropertyChange("contact4Email", oldContact4Email, contact4Email);
    }
    
    public String getContact4Phone() {
        return contact4Phone;
    }

    public void setContact4Phone(String contact4Phone) {
        String oldContact4Phone = this.contact4Phone;
        this.contact4Phone = contact4Phone;
        changeSupport.firePropertyChange("contact4Phone", oldContact4Phone, contact4Phone);
    }
    
    public String getContact4Function() {
        return contact4Function;
    }

    public void setContact4Function(String contact4Function) {
        String oldContact4Function = this.contact4Function;
        this.contact4Function = contact4Function;
        changeSupport.firePropertyChange("contact4Function", oldContact4Function, contact4Function);
    }
    
    public String getContact5Name() {
        return contact5Name;
    }

    public void setContact5Name(String contact5Name) {
        String oldContact5Name = this.contact5Name;
        this.contact5Name = contact5Name;
        changeSupport.firePropertyChange("contact5Name", oldContact5Name, contact5Name);
    }
    
    public String getContact5Email() {
        return contact5Email;
    }

    public void setContact5Email(String contact5Email) {
        String oldContact5Email = this.contact5Email;
        this.contact5Email = contact5Email;
        changeSupport.firePropertyChange("contact5Email", oldContact5Email, contact5Email);
    }
    
    public String getContact5Phone() {
        return contact5Phone;
    }

    public void setContact5Phone(String contact5Phone) {
        String oldContact5Phone = this.contact5Phone;
        this.contact5Phone = contact5Phone;
        changeSupport.firePropertyChange("contact5Phone", oldContact5Phone, contact5Phone);
    }
    
    public String getContact5Function() {
        return contact5Function;
    }

    public void setContact5Function(String contact5Function) {
        String oldContact5Function = this.contact5Function;
        this.contact5Function = contact5Function;
        changeSupport.firePropertyChange("contact5Function", oldContact5Function, contact5Function);
    }
    
    public String getContact6Name() {
        return contact6Name;
    }

    public void setContact6Name(String contact6Name) {
        String oldContact6Name = this.contact6Name;
        this.contact6Name = contact6Name;
        changeSupport.firePropertyChange("contact6Name", oldContact6Name, contact6Name);
    }
    
    public String getContact6Email() {
        return contact6Email;
    }

    public void setContact6Email(String contact6Email) {
        String oldContact6Email = this.contact6Email;
        this.contact6Email = contact6Email;
        changeSupport.firePropertyChange("contact6Email", oldContact6Email, contact6Email);
    }
    
    public String getContact6Phone() {
        return contact6Phone;
    }

    public void setContact6Phone(String contact6Phone) {
        String oldContact6Phone = this.contact6Phone;
        this.contact6Phone = contact6Phone;
        changeSupport.firePropertyChange("contact6Phone", oldContact6Phone, contact6Phone);
    }
    
    public String getContact6Function() {
        return contact6Function;
    }

    public void setContact6Function(String contact6Function) {
        String oldContact6Function = this.contact6Function;
        this.contact6Function = contact6Function;
        changeSupport.firePropertyChange("contact6Function", oldContact6Function, contact6Function);
    }
    
    public String getBuyer() {
        return buyer;
    }

    public void setBuyer(String buyer) {
        String oldBuyer = this.buyer;
        this.buyer = buyer;
        changeSupport.firePropertyChange("buyer", oldBuyer, buyer);
    }

    public String getQm() {
        return qm;
    }

    public void setQm(String qm) {
        String oldQm = this.qm;
        this.qm = qm;
        changeSupport.firePropertyChange("qm", oldQm, qm);
    }

    public String getBsciPart() {
        return bsciPart;
    }

    public void setBsciPart(String bsciPart) {
        String oldBsciPart = this.bsciPart;
        this.bsciPart = bsciPart;
        changeSupport.firePropertyChange("bsciPart", oldBsciPart, bsciPart);
    }

    public String getBsciResult() {
        return bsciResult;
    }

    public void setBsciResult(String bsciResult) {
        String oldBsciResult = this.bsciResult;
        this.bsciResult = bsciResult;
        changeSupport.firePropertyChange("bsciResult", oldBsciResult, bsciResult);
    }

    public Date getBsciFrom() {
        return bsciFrom;
    }

    public void setBsciFrom(Date bsciFrom) {
        Date oldBsciFrom = this.bsciFrom;
        this.bsciFrom = bsciFrom;
        changeSupport.firePropertyChange("bsciFrom", oldBsciFrom, bsciFrom);
    }

    public Date getBsciTill() {
        return bsciTill;
    }

    public void setBsciTill(Date bsciTill) {
        Date oldBsciTill = this.bsciTill;
        this.bsciTill = bsciTill;
        changeSupport.firePropertyChange("bsciTill", oldBsciTill, bsciTill);
    }

    public String getCertOther1() {
        return certOther1;
    }

    public void setCertOther1(String certOther1) {
        String oldcertOther1 = this.certOther1;
        this.certOther1 = certOther1;
        changeSupport.firePropertyChange("certOther1", oldcertOther1, certOther1);
    }

    public String getCertOther2() {
        return certOther2;
    }

    public void setCertOther2(String certOther2) {
        String oldcertOther2 = this.certOther2;
        this.certOther2 = certOther2;
        changeSupport.firePropertyChange("certOther2", oldcertOther2, certOther2);
    }

    public String getBsciOther1Name() {
        return bsciOther1Name;
    }

    public void setBsciOther1Name(String bsciOther1Name) {
        String oldBsciOther1Name = this.bsciOther1Name;
        this.bsciOther1Name = bsciOther1Name;
        changeSupport.firePropertyChange("bsciOther1Name", oldBsciOther1Name, bsciOther1Name);
    }

    public Date getBsciOther1From() {
        return bsciOther1From;
    }

    public void setBsciOther1From(Date bsciOther1From) {
        Date oldBsciOther1From = this.bsciOther1From;
        this.bsciOther1From = bsciOther1From;
        changeSupport.firePropertyChange("bsciOther1From", oldBsciOther1From, bsciOther1From);
    }

    public Date getBsciOther1Till() {
        return bsciOther1Till;
    }

    public void setBsciOther1Till(Date bsciOther1Till) {
        Date oldBsciOther1Till = this.bsciOther1Till;
        this.bsciOther1Till = bsciOther1Till;
        changeSupport.firePropertyChange("bsciOther1Till", oldBsciOther1Till, bsciOther1Till);
    }

    public String getBsciOther2Name() {
        return bsciOther2Name;
    }

    public void setBsciOther2Name(String bsciOther2Name) {
        String oldBsciOther2Name = this.bsciOther2Name;
        this.bsciOther2Name = bsciOther2Name;
        changeSupport.firePropertyChange("bsciOther2Name", oldBsciOther2Name, bsciOther2Name);
    }

    public Date getBsciOther2From() {
        return bsciOther2From;
    }

    public void setBsciOther2From(Date bsciOther2From) {
        Date oldBsciOther2From = this.bsciOther2From;
        this.bsciOther2From = bsciOther2From;
        changeSupport.firePropertyChange("bsciOther2From", oldBsciOther2From, bsciOther2From);
    }

    public Date getBsciOther2Till() {
        return bsciOther2Till;
    }

    public void setBsciOther2Till(Date bsciOther2Till) {
        Date oldBsciOther2Till = this.bsciOther2Till;
        this.bsciOther2Till = bsciOther2Till;
        changeSupport.firePropertyChange("bsciOther2Till", oldBsciOther2Till, bsciOther2Till);
    }

    public String getBsciOther3Name() {
        return bsciOther3Name;
    }

    public void setBsciOther3Name(String bsciOther3Name) {
        String oldBsciOther3Name = this.bsciOther3Name;
        this.bsciOther3Name = bsciOther3Name;
        changeSupport.firePropertyChange("bsciOther3Name", oldBsciOther3Name, bsciOther3Name);
    }

    public Date getBsciOther3From() {
        return bsciOther3From;
    }

    public void setBsciOther3From(Date bsciOther3From) {
        Date oldBsciOther3From = this.bsciOther3From;
        this.bsciOther3From = bsciOther3From;
        changeSupport.firePropertyChange("bsciOther3From", oldBsciOther3From, bsciOther3From);
    }

    public Date getBsciOther3Till() {
        return bsciOther3Till;
    }

    public void setBsciOther3Till(Date bsciOther3Till) {
        Date oldBsciOther3Till = this.bsciOther3Till;
        this.bsciOther3Till = bsciOther3Till;
        changeSupport.firePropertyChange("bsciOther3Till", oldBsciOther3Till, bsciOther3Till);
    }

    public String getCertIso9000() {
        return certIso9000;
    }

    public void setCertIso9000(String certIso9000) {
        String oldCertIso9000 = this.certIso9000;
        this.certIso9000 = certIso9000;
        changeSupport.firePropertyChange("certIso9000", oldCertIso9000, certIso9000);
    }

    public Date getCertIso9000From() {
        return certIso9000From;
    }

    public void setCertIso9000From(Date certIso9000From) {
        Date oldCertIso9000From = this.certIso9000From;
        this.certIso9000From = certIso9000From;
        changeSupport.firePropertyChange("certIso9000From", oldCertIso9000From, certIso9000From);
    }

    public Date getCertIso9000Till() {
        return certIso9000Till;
    }

    public void setCertIso9000Till(Date certIso9000Till) {
        Date oldCertIso9000Till = this.certIso9000Till;
        this.certIso9000Till = certIso9000Till;
        changeSupport.firePropertyChange("certIso9000Till", oldCertIso9000Till, certIso9000Till);
    }

    public String getCertIso9000Name() {
        return certIso9000Name;
    }

    public void setCertIso9000Name(String certIso9000Name) {
        String oldCertIso9000Name = this.certIso9000Name;
        this.certIso9000Name = certIso9000Name;
        changeSupport.firePropertyChange("certIso9000Name", oldCertIso9000Name, certIso9000Name);
    }

    public boolean getDeclRohs() {
        return declRohs;
    }

    public void setDeclRohs(boolean declRohs) {
        boolean olddeclRohs = this.declRohs;
        this.declRohs = declRohs;
        changeSupport.firePropertyChange("declRohs", olddeclRohs, declRohs);
    }

    public String getCertIso14000() {
        return certIso14000;
    }

    public void setCertIso14000(String certIso14000) {
        String oldCertIso14000 = this.certIso14000;
        this.certIso14000 = certIso14000;
        changeSupport.firePropertyChange("certIso14000", oldCertIso14000, certIso14000);
    }

    public Date getCertIso14000From() {
        return certIso14000From;
    }

    public void setCertIso14000From(Date certIso14000From) {
        Date oldCertIso14000From = this.certIso14000From;
        this.certIso14000From = certIso14000From;
        changeSupport.firePropertyChange("certIso14000From", oldCertIso14000From, certIso14000From);
    }

    public Date getCertIso14000Till() {
        return certIso14000Till;
    }

    public void setCertIso14000Till(Date certIso14000Till) {
        Date oldCertIso14000Till = this.certIso14000Till;
        this.certIso14000Till = certIso14000Till;
        changeSupport.firePropertyChange("certIso14000Till", oldCertIso14000Till, certIso14000Till);
    }

    public String getCertIso14000Name() {
        return certIso14000Name;
    }

    public void setCertIso14000Name(String certIso14000Name) {
        String oldCertIso14000Name = this.certIso14000Name;
        this.certIso14000Name = certIso14000Name;
        changeSupport.firePropertyChange("certIso14000Name", oldCertIso14000Name, certIso14000Name);
    }

    public boolean getDeclReach() {
        return declReach;
    }

    public void setDeclReach(boolean declReach) {
        boolean olddeclReach = this.declReach;
        this.declReach = declReach;
        changeSupport.firePropertyChange("declReach", olddeclReach, declReach);
    }

    public Date getCertOther1From() {
        return certOther1From;
    }

    public void setCertOther1From(Date certOther1From) {
        Date oldCertOther1From = this.certOther1From;
        this.certOther1From = certOther1From;
        changeSupport.firePropertyChange("certOther1From", oldCertOther1From, certOther1From);
    }

    public Date getCertOther1Till() {
        return certOther1Till;
    }

    public void setCertOther1Till(Date certOther1Till) {
        Date oldCertOther1Till = this.certOther1Till;
        this.certOther1Till = certOther1Till;
        changeSupport.firePropertyChange("certOther1Till", oldCertOther1Till, certOther1Till);
    }

    public String getCertOther1Name() {
        return certOther1Name;
    }

    public void setCertOther1Name(String certOther1Name) {
        String oldCertOther1Name = this.certOther1Name;
        this.certOther1Name = certOther1Name;
        changeSupport.firePropertyChange("certOther1Name", oldCertOther1Name, certOther1Name);
    }

    public Date getCertOther2From() {
        return certOther2From;
    }

    public void setCertOther2From(Date certOther2From) {
        Date oldCertOther2From = this.certOther2From;
        this.certOther2From = certOther2From;
        changeSupport.firePropertyChange("certOther2From", oldCertOther2From, certOther2From);
    }

    public Date getCertOther2Till() {
        return certOther2Till;
    }

    public void setCertOther2Till(Date certOther2Till) {
        Date oldCertOther2Till = this.certOther2Till;
        this.certOther2Till = certOther2Till;
        changeSupport.firePropertyChange("certOther2Till", oldCertOther2Till, certOther2Till);
    }

    public String getCertOther2Name() {
        return certOther2Name;
    }

    public void setCertOther2Name(String certOther2Name) {
        String oldCertOther2Name = this.certOther2Name;
        this.certOther2Name = certOther2Name;
        changeSupport.firePropertyChange("certOther2Name", oldCertOther2Name, certOther2Name);
    }

    public Date getDeclReachDate() {
        return declReachDate;
    }

    public void setDeclReachDate(Date declReachDate) {
        Date olddeclReachDate = this.declReachDate;
        this.declReachDate = declReachDate;
        changeSupport.firePropertyChange("declReachDate", olddeclReachDate, declReachDate);
    }
    
    public String getCertOther3() {
        return certOther3;
    }

    public void setCertOther3(String certOther3) {
        String oldCertOther3 = this.certOther3;
        this.certOther3 = certOther3;
        changeSupport.firePropertyChange("certOther3", oldCertOther3, certOther3);
    }
    
    public Date getCertOther3From() {
        return certOther3From;
    }

    public void setCertOther3From(Date certOther3From) {
        Date oldCertOther3From = this.certOther3From;
        this.certOther3From = certOther3From;
        changeSupport.firePropertyChange("certOther3From", oldCertOther3From, certOther3From);
    }

    public Date getCertOther3Till() {
        return certOther3Till;
    }

    public void setCertOther3Till(Date certOther3Till) {
        Date oldCertOther3Till = this.certOther3Till;
        this.certOther3Till = certOther3Till;
        changeSupport.firePropertyChange("certOther3Till", oldCertOther3Till, certOther3Till);
    }

    public String getCertOther3Name() {
        return certOther3Name;
    }

    public void setCertOther3Name(String certOther3Name) {
        String oldCertOther3Name = this.certOther3Name;
        this.certOther3Name = certOther3Name;
        changeSupport.firePropertyChange("certOther3Name", oldCertOther3Name, certOther3Name);
    }
    
    public String getCertOther4() {
        return certOther4;
    }

    public void setCertOther4(String certOther4) {
        String oldCertOther4 = this.certOther4;
        this.certOther4 = certOther4;
        changeSupport.firePropertyChange("certOther4", oldCertOther4, certOther4);
    }
    
    public Date getCertOther4From() {
        return certOther4From;
    }

    public void setCertOther4From(Date certOther4From) {
        Date oldCertOther4From = this.certOther4From;
        this.certOther4From = certOther4From;
        changeSupport.firePropertyChange("certOther4From", oldCertOther4From, certOther4From);
    }

    public Date getCertOther4Till() {
        return certOther4Till;
    }

    public void setCertOther4Till(Date certOther4Till) {
        Date oldCertOther4Till = this.certOther4Till;
        this.certOther4Till = certOther4Till;
        changeSupport.firePropertyChange("certOther4Till", oldCertOther4Till, certOther4Till);
    }

    public String getCertOther4Name() {
        return certOther4Name;
    }

    public void setCertOther4Name(String certOther4Name) {
        String oldCertOther4Name = this.certOther4Name;
        this.certOther4Name = certOther4Name;
        changeSupport.firePropertyChange("certOther4Name", oldCertOther4Name, certOther4Name);
    }

    public String getCertOther5() {
        return certOther5;
    }

    public void setCertOther5(String certOther5) {
        String oldCertOther5 = this.certOther5;
        this.certOther5 = certOther5;
        changeSupport.firePropertyChange("certOther5", oldCertOther5, certOther5);
    }
    
    public Date getCertOther5From() {
        return certOther5From;
    }

    public void setCertOther5From(Date certOther5From) {
        Date oldCertOther5From = this.certOther5From;
        this.certOther5From = certOther5From;
        changeSupport.firePropertyChange("certOther5From", oldCertOther5From, certOther5From);
    }

    public Date getCertOther5Till() {
        return certOther5Till;
    }

    public void setCertOther5Till(Date certOther5Till) {
        Date oldCertOther5Till = this.certOther5Till;
        this.certOther5Till = certOther5Till;
        changeSupport.firePropertyChange("certOther5Till", oldCertOther5Till, certOther5Till);
    }

    public String getCertOther5Name() {
        return certOther5Name;
    }

    public void setCertOther5Name(String certOther5Name) {
        String oldCertOther5Name = this.certOther5Name;
        this.certOther5Name = certOther5Name;
        changeSupport.firePropertyChange("certOther5Name", oldCertOther5Name, certOther5Name);
    }

    public String getCertOther6() {
        return certOther6;
    }

    public void setCertOther6(String certOther6) {
        String oldCertOther6 = this.certOther6;
        this.certOther6 = certOther6;
        changeSupport.firePropertyChange("certOther6", oldCertOther6, certOther6);
    }
    
    public Date getCertOther6From() {
        return certOther6From;
    }

    public void setCertOther6From(Date certOther6From) {
        Date oldCertOther6From = this.certOther6From;
        this.certOther6From = certOther6From;
        changeSupport.firePropertyChange("certOther6From", oldCertOther6From, certOther6From);
    }

    public Date getCertOther6Till() {
        return certOther6Till;
    }

    public void setCertOther6Till(Date certOther6Till) {
        Date oldCertOther6Till = this.certOther6Till;
        this.certOther6Till = certOther6Till;
        changeSupport.firePropertyChange("certOther6Till", oldCertOther6Till, certOther6Till);
    }

    public String getCertOther6Name() {
        return certOther6Name;
    }

    public void setCertOther6Name(String certOther6Name) {
        String oldcertOther6Name = this.certOther6Name;
        this.certOther6Name = certOther6Name;
        changeSupport.firePropertyChange("certOther6Name", oldcertOther6Name, certOther6Name);
    }

    public String getCertOther7() {
        return certOther7;
    }

    public void setCertOther7(String certOther7) {
        String oldCertOther7 = this.certOther7;
        this.certOther7 = certOther7;
        changeSupport.firePropertyChange("certOther7", oldCertOther7, certOther7);
    }
    
    public Date getCertOther7From() {
        return certOther7From;
    }

    public void setCertOther7From(Date certOther7From) {
        Date oldCertOther7From = this.certOther7From;
        this.certOther7From = certOther7From;
        changeSupport.firePropertyChange("certOther7From", oldCertOther7From, certOther7From);
    }

    public Date getCertOther7Till() {
        return certOther7Till;
    }

    public void setCertOther7Till(Date certOther7Till) {
        Date oldCertOther7Till = this.certOther7Till;
        this.certOther7Till = certOther7Till;
        changeSupport.firePropertyChange("certOther7Till", oldCertOther7Till, certOther7Till);
    }

    public String getCertOther7Name() {
        return certOther7Name;
    }

    public void setCertOther7Name(String certOther7Name) {
        String oldcertOther7Name = this.certOther7Name;
        this.certOther7Name = certOther7Name;
        changeSupport.firePropertyChange("certOther7Name", oldcertOther7Name, certOther7Name);
    }

    public String getCertOther8() {
        return certOther8;
    }

    public void setCertOther8(String certOther8) {
        String oldCertOther8 = this.certOther8;
        this.certOther8 = certOther8;
        changeSupport.firePropertyChange("certOther8", oldCertOther8, certOther8);
    }
    
    public Date getCertOther8From() {
        return certOther8From;
    }

    public void setCertOther8From(Date certOther8From) {
        Date oldCertOther8From = this.certOther8From;
        this.certOther8From = certOther8From;
        changeSupport.firePropertyChange("certOther8From", oldCertOther8From, certOther8From);
    }

    public Date getCertOther8Till() {
        return certOther8Till;
    }

    public void setCertOther8Till(Date certOther8Till) {
        Date oldCertOther8Till = this.certOther8Till;
        this.certOther8Till = certOther8Till;
        changeSupport.firePropertyChange("certOther8Till", oldCertOther8Till, certOther8Till);
    }

    public String getCertOther8Name() {
        return certOther8Name;
    }

    public void setCertOther8Name(String certOther8Name) {
        String oldcertOther8Name = this.certOther8Name;
        this.certOther8Name = certOther8Name;
        changeSupport.firePropertyChange("certOther8Name", oldcertOther8Name, certOther8Name);
    }

    public String getCertOther9() {
        return certOther9;
    }

    public void setCertOther9(String certOther9) {
        String oldCertOther9 = this.certOther9;
        this.certOther9 = certOther9;
        changeSupport.firePropertyChange("certOther9", oldCertOther9, certOther9);
    }
    
    public Date getCertOther9From() {
        return certOther9From;
    }

    public void setCertOther9From(Date certOther9From) {
        Date oldCertOther9From = this.certOther9From;
        this.certOther9From = certOther9From;
        changeSupport.firePropertyChange("certOther9From", oldCertOther9From, certOther9From);
    }

    public Date getCertOther9Till() {
        return certOther9Till;
    }

    public void setCertOther9Till(Date certOther9Till) {
        Date oldCertOther9Till = this.certOther9Till;
        this.certOther9Till = certOther9Till;
        changeSupport.firePropertyChange("certOther9Till", oldCertOther9Till, certOther9Till);
    }

    public String getCertOther9Name() {
        return certOther9Name;
    }

    public void setCertOther9Name(String certOther9Name) {
        String oldcertOther9Name = this.certOther9Name;
        this.certOther9Name = certOther9Name;
        changeSupport.firePropertyChange("certOther9Name", oldcertOther9Name, certOther9Name);
    }

    public String getCertOther10() {
        return certOther10;
    }

    public void setCertOther10(String certOther10) {
        String oldCertOther10 = this.certOther10;
        this.certOther10 = certOther10;
        changeSupport.firePropertyChange("certOther10", oldCertOther10, certOther10);
    }
    
    public Date getCertOther10From() {
        return certOther10From;
    }

    public void setCertOther10From(Date certOther10From) {
        Date oldCertOther10From = this.certOther10From;
        this.certOther10From = certOther10From;
        changeSupport.firePropertyChange("certOther10From", oldCertOther10From, certOther10From);
    }

    public Date getCertOther10Till() {
        return certOther10Till;
    }

    public void setCertOther10Till(Date certOther10Till) {
        Date oldCertOther10Till = this.certOther10Till;
        this.certOther10Till = certOther10Till;
        changeSupport.firePropertyChange("certOther10Till", oldCertOther10Till, certOther10Till);
    }

    public String getCertOther10Name() {
        return certOther10Name;
    }

    public void setCertOther10Name(String certOther10Name) {
        String oldcertOther10Name = this.certOther10Name;
        this.certOther10Name = certOther10Name;
        changeSupport.firePropertyChange("certOther10Name", oldcertOther10Name, certOther10Name);
    }

    public Date getDeclRohsDate() {
        return declRohsDate;
    }

    public void setDeclRohsDate(Date declRohsDate) {
        Date olddeclRohsDate = this.declRohsDate;
        this.declRohsDate = declRohsDate;
        changeSupport.firePropertyChange("declRohsDate", olddeclRohsDate, declRohsDate);
    }

    public Date getDeclSdaDate() {
        return declSdaDate;
    }

    public void setDeclSdaDate(Date declSdaDate) {
        Date olddeclSdaDate = this.declSdaDate;
        this.declSdaDate = declSdaDate;
        changeSupport.firePropertyChange("declSdaDate", olddeclSdaDate, declSdaDate);
    }

    public boolean getDeclSda() {
        return declSda;
    }

    public void setDeclSda(boolean declSda) {
        boolean olddeclSda = this.declSda;
        this.declSda = declSda;
        changeSupport.firePropertyChange("declSda", olddeclSda, declSda);
    }

    public Date getDeclSopDate() {
        return declSopDate;
    }

    public void setDeclSopDate(Date declSopDate) {
        Date olddeclSopDate = this.declSopDate;
        this.declSopDate = declSopDate;
        changeSupport.firePropertyChange("declSopDate", olddeclSopDate, declSopDate);
    }

    public boolean getDeclSop() {
        return declSop;
    }

    public void setDeclSop(boolean declSop) {
        boolean olddeclSop = this.declSop;
        this.declSop = declSop;
        changeSupport.firePropertyChange("declSop", olddeclSop, declSop);
    }

    public Date getDeclBrandDate() {
        return declBrandDate;
    }

    public void setDeclBrandDate(Date declBrandDate) {
        Date oldDeclBrandDate = this.declBrandDate;
        this.declBrandDate = declBrandDate;
        changeSupport.firePropertyChange("declBrandDate", oldDeclBrandDate, declBrandDate);
    }

    public Date getDeclPackDate() {
        return declPackDate;
    }

    public void setDeclPackDate(Date declPackDate) {
        Date oldDeclPackDate = this.declPackDate;
        this.declPackDate = declPackDate;
        changeSupport.firePropertyChange("declPackDate", oldDeclPackDate, declPackDate);
    }

    public Date getDeclContrDate() {
        return declContrDate;
    }

    public void setDeclContrDate(Date declContrDate) {
        Date oldDeclContrDate = this.declContrDate;
        this.declContrDate = declContrDate;
        changeSupport.firePropertyChange("declContrDate", oldDeclContrDate, declContrDate);
    }

    public String getDeclWarranty() {
        return declWarranty;
    }

    public void setDeclWarranty(String declWarranty) {
        String oldDeclWarranty = this.declWarranty;
        this.declWarranty = declWarranty;
        changeSupport.firePropertyChange("declWarranty", oldDeclWarranty, declWarranty);
    }

    public String getDeclPay() {
        return declPay;
    }

    public void setDeclPay(String declPay) {
        String oldDeclPay = this.declPay;
        this.declPay = declPay;
        changeSupport.firePropertyChange("declPay", oldDeclPay, declPay);
    }

    public boolean getDeclBrand() {
        return declBrand;
    }

    public void setDeclBrand(boolean declBrand) {
        boolean oldDeclBrand = this.declBrand;
        this.declBrand = declBrand;
        changeSupport.firePropertyChange("declBrand", oldDeclBrand, declBrand);
    }

    public boolean getDeclPack() {
        return declPack;
    }

    public void setDeclPack(boolean declPack) {
        boolean oldDeclPack = this.declPack;
        this.declPack = declPack;
        changeSupport.firePropertyChange("declPack", oldDeclPack, declPack);
    }

    public boolean getDeclContr() {
        return declContr;
    }

    public void setDeclContr(boolean declContr) {
        boolean oldDeclContr = this.declContr;
        this.declContr = declContr;
        changeSupport.firePropertyChange("declContr", oldDeclContr, declContr);
    }

    public String getfolder() {
        return folder;
    }

    public void setfolder(String folder) {
        String oldfolder = this.folder;
        this.folder = folder;
        changeSupport.firePropertyChange("folder", oldfolder, folder);
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (supplier != null ? supplier.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Suppliers)) {
            return false;
        }
        Suppliers other = (Suppliers) object;
        if ((this.supplier == null && other.supplier != null) || (this.supplier != null && !this.supplier.equals(other.supplier))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "suppliers.Suppliers[ supplier=" + supplier + " ]";
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}

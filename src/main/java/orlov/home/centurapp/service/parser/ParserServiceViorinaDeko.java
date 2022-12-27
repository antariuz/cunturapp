package orlov.home.centurapp.service.parser;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import orlov.home.centurapp.dto.OpencartDto;
import orlov.home.centurapp.entity.app.*;
import orlov.home.centurapp.entity.opencart.CategoryDescriptionOpencart;
import orlov.home.centurapp.entity.opencart.CategoryOpencart;
import orlov.home.centurapp.entity.opencart.ProductDescriptionOpencart;
import orlov.home.centurapp.entity.opencart.ProductOpencart;
import orlov.home.centurapp.service.api.translate.TranslateService;
import orlov.home.centurapp.service.appservice.FileService;
import orlov.home.centurapp.service.appservice.ScraperDataUpdateService;
import orlov.home.centurapp.service.daoservice.app.AppDaoService;
import orlov.home.centurapp.service.daoservice.opencart.OpencartDaoService;
import orlov.home.centurapp.util.AppConstant;
import orlov.home.centurapp.util.OCConstant;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ParserServiceViorinaDeko extends ParserServiceAbstract {

    private static final String SUPPLIER_URL = "http://viorina-deko.com/ua/";
    private static final String SUPPLIER_NAME = "viorina-deko"; //fixme: clarify
    private static final String DISPLAY_NAME = "ВIОРИНА-ДЕКО"; //fixme: clarify
    private static final String MANUFACTURER_NAME = "VIORINA-DEKO"; //fixme: clarify

    private final AppDaoService appDaoService;
    private final OpencartDaoService opencartDaoService;

    public ParserServiceViorinaDeko(AppDaoService appDaoService, OpencartDaoService opencartDaoService, ScraperDataUpdateService scraperDataUpdateService, TranslateService translateService, FileService fileService) {
        super(appDaoService, opencartDaoService, scraperDataUpdateService, translateService, fileService);
        this.appDaoService = appDaoService;
        this.opencartDaoService = opencartDaoService;
    }

    @Override
    public void doProcess() {
        try {
            Timestamp startProcess = new Timestamp(Calendar.getInstance().getTime().getTime());

            SupplierApp supplierApp = buildSupplierApp(SUPPLIER_NAME, DISPLAY_NAME, SUPPLIER_URL);
            List<CategoryOpencart> siteCategories = getSiteCategories(supplierApp);
            List<ProductOpencart> productsFromSite = getProductsInitDataByCategory(siteCategories, supplierApp);

            OpencartDto opencartInfo = getOpencartInfo(productsFromSite, supplierApp);

            checkPrice(opencartInfo, supplierApp);
            List<ProductOpencart> fullProductsData = getFullProductsData(opencartInfo.getNewProduct(), supplierApp);
            fullProductsData
                    .forEach(opencartDaoService::saveProductOpencart);
            updateProductSupplierOpencartBySupplierApp(supplierApp);
            Timestamp endProcess = new Timestamp(Calendar.getInstance().getTime().getTime());

            OrderProcessApp orderProcessApp = opencartInfo.getOrderProcessApp();
            orderProcessApp.setStartProcess(startProcess);
            orderProcessApp.setEndProcess(endProcess);
            appDaoService.saveOrderDataApp(orderProcessApp);
        } catch (Exception ex) {
            log.warn("Exception parsing viorina-deko", ex);
        }
    }

    @Override
    public List<CategoryOpencart> getSiteCategories(SupplierApp supplierApp) {

        final String MAIN_MENU_HREF_PATH = "dytyachi_mebli";

        List<CategoryOpencart> supplierCategoryOpencartDB = supplierApp.getCategoryOpencartDB();
        Document doc = getWebDocument(supplierApp.getUrl(), new HashMap<>());

        if (Objects.nonNull(doc)) {
            List<CategoryOpencart> mainCategories = doc.select("div#menu ul li>a").stream()
                    .filter(element -> element.attr("href").contains(MAIN_MENU_HREF_PATH))
                    .map(element -> {
                        String categoryUrl = element.attr("href");
                        String title = element.text().trim();
                        CategoryOpencart categoryOpencart = new CategoryOpencart.Builder()
                                .withUrl(categoryUrl)
                                .withParentCategory(supplierApp.getMainSupplierCategory())
                                .withParentId(supplierApp.getMainSupplierCategory().getCategoryId())
                                .withTop(false) // TODO: wtf?
                                .withStatus(false) // TODO: wtf?
                                .build();

                        CategoryDescriptionOpencart description = new CategoryDescriptionOpencart.Builder()
                                .withName(title)
                                .withDescription(supplierApp.getName())
                                .withLanguageId(OCConstant.UA_LANGUAGE_ID)
                                .build();
                        categoryOpencart.getDescriptions().add(description);
                        return categoryOpencart;
                    })
                    .peek(c -> log.info("Name category: {} url: {}", c.getDescriptions().get(0).getName(), c.getUrl()))
                    .collect(Collectors.toList());
            log.info("Main categories size: {}", mainCategories.size());

            List<CategoryOpencart> siteCategoryStructure = mainCategories
                    .stream()
                    .map(this::recursiveWalkSiteCategory)
                    .collect(Collectors.toList());

            //subCategories. Final structure with parents
            List<CategoryOpencart> siteCategoryList = siteCategoryStructure
                    .stream()
                    .map(sc -> recursiveCollectListCategory(sc, supplierCategoryOpencartDB))
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
            siteCategoryList.add(supplierApp.getMainSupplierCategory());
            siteCategoryList.add(supplierApp.getGlobalSupplierCategory());
            return siteCategoryList;
        }
        return null;
    }

    @Override
    public CategoryOpencart recursiveWalkSiteCategory(CategoryOpencart category) {
        String url = category.getUrl();
        Document doc = getWebDocument(url, new HashMap<>());
        log.info("Get subcategories of category: {}", category.getDescriptions().get(0).getName());
        if (Objects.nonNull(doc)) {
            List<CategoryOpencart> subCategories = doc.select("div#list div.block div.content>a")
                    .stream()
                    .filter(element ->
                            !element.select("a").attr("href").contains("NAShI-PARTNERY")
                                    && !element.select("a").attr("href").contains("Kataloh-Viorina---Deko"))
                    .map(element -> {
                        final String fullUriToCategory = element.select("a").attr("href");
                        final String title = element.select("a>h2").text();
                        log.info("Sub href: {}", fullUriToCategory);
                        log.info("Sub title: {}", title);
                        CategoryOpencart subCategory = new CategoryOpencart.Builder()
                                .withUrl(fullUriToCategory)
                                .withTop(false)
                                .withParentCategory(category)
                                .withStatus(false)
                                .build();
                        CategoryDescriptionOpencart description = new CategoryDescriptionOpencart.Builder()
                                .withName(title)
                                .withDescription(category.getDescriptions().get(0).getDescription())
                                .withLanguageId(OCConstant.UA_LANGUAGE_ID)
                                .build();
                        subCategory.getDescriptions().add(description);
                        return subCategory;
                    })
                    .collect(Collectors.toList());
            category.getCategoriesOpencart().addAll(subCategories);
        }
        return category;
    }

    @Override
    public List<ProductOpencart> getProductsInitDataByCategory(List<CategoryOpencart> categoriesWithProduct, SupplierApp supplierApp) {
        List<ProductOpencart> productsCategory = new ArrayList<>();

        for (CategoryOpencart c : categoriesWithProduct) {
            int categoryId = c.getCategoryId();
            List<CategoryOpencart> childrenCategory = categoriesWithProduct.stream().filter(sub -> sub.getParentId() == categoryId).collect(Collectors.toList());
            if (childrenCategory.isEmpty()) {
                String name = c.getDescriptions().get(0).getName();
                CategoryApp categoryApp = getCategoryApp(name, supplierApp);

                String url = c.getUrl();
                log.info("Get info by url: {}", url);
                Document doc = getWebDocument(url, new HashMap<>());
                log.info("Go to page: {}", url);

                List<CategoryOpencart> parentsCategories = getParentsCategories(c, categoriesWithProduct);
                log.info("parentsCategories size: {}", parentsCategories.size());

                if (Objects.nonNull(doc)) {
                    Elements dirtyElements = doc.select("div#list div.block div.content>a");
                    Elements elementsProduct = new Elements();
                    dirtyElements.forEach(element -> {
                        if (!element.select("a").attr("href").contains("Palitra-koloriv-lizhka-Velyur-")
                                && !element.select("a").attr("href").contains("Kimnata-TAKhO-/")) {
                            elementsProduct.add(element);
                        }
                    });
                    if (!doc.select("div.paginator.mT40>a").isEmpty()) {
                        log.info("Pagination has been found. Getting additional products:");
                        List<String> paginationUrls = doc.select("div.paginator.mT40>a").stream()
                                .filter(element -> !element.text().equals("»"))
                                .map(element -> element.attr("href"))
                                .collect(Collectors.toList());
                        paginationUrls.forEach(paginationUrl -> {
                            log.info("Get info by url: {}", paginationUrl);
                            Document paginationDoc = getWebDocument(paginationUrl, new HashMap<>());
                            log.info("Go to page: {}", paginationUrl);
                            if (Objects.nonNull(paginationDoc)) {
                                Elements additionalProductElements = paginationDoc.select("div#list div.block div.content>a");
                                elementsProduct.addAll(additionalProductElements);
                            }
                        });
                    }

                    log.info("elementsProduct: {}", elementsProduct.size());

                    elementsProduct.forEach(productElement -> {

                        String productUrl = productElement.select("a").attr("href");
                        log.info("Product url: {}", productUrl);

                        String productTitle = productElement.select("a>h2").text().trim();
                        log.info("Product title: {}", productTitle);

                        String productSku = getSKUFromHref(productUrl);
                        log.info("Product sku: {}", productSku);

                        String price = "0.0";
                        BigDecimal priceNumberFree = new BigDecimal("0.0");
                        Document productDocument = getWebDocument(productUrl, new HashMap<>());
                        if (Objects.nonNull(productDocument)) {

                            Elements priceElement = productDocument.select("div.price span.color-price");
                            if (priceElement.text().startsWith("від")) {
                                price = priceElement.text().replace("від", "");
                            } else {
                                price = priceElement.text();
                            }
                            if (price.endsWith(",")) {
                                price = price.concat("0");
                            }
                            if (price.contains(",")) {
                                price = price.replace(",", ".");
                            }
                            while (price.contains(" ")) {
                                price = price.replace(" ", "");
                            }
                            log.info("Product price: {}", price);
                            priceNumberFree = new BigDecimal(price).setScale(2, RoundingMode.UP);
                            log.info("Product priceNumberFree: {}", priceNumberFree);

                        }

                        ProductProfileApp productProfileApp = new ProductProfileApp.Builder()
                                .withSupplierId(supplierApp.getSupplierAppId())
                                .withSupplierApp(supplierApp)
                                .withCategoryId(categoryApp.getCategoryId())
                                .withCategoryApp(categoryApp)
                                .withUrl(productUrl)
                                .withSku(productSku)
                                .withTitle(productTitle)
                                .withPrice(priceNumberFree)
                                .build();

                        ProductOpencart product = new ProductOpencart.Builder()
                                .withProductProfileApp(productProfileApp)
                                .withUrlProduct(productUrl)
                                .withTitle(productTitle)
                                .withSku(productSku)
                                .withJan(supplierApp.getName())
                                .withPrice(priceNumberFree)
                                .withItuaOriginalPrice(priceNumberFree)
                                .build();

                        product.setCategoriesOpencart(parentsCategories);

                        ProductOpencart prodFromList = productsCategory
                                .stream()
                                .filter(ps -> ps.getSku().equals(product.getSku()))
                                .findFirst()
                                .orElse(null);

                        //if product exist in two different categories
                        if (Objects.nonNull(prodFromList)) {
                            List<CategoryOpencart> categoriesOpencart = prodFromList.getCategoriesOpencart();
                            List<CategoryOpencart> newCategoriesOpencart = product.getCategoriesOpencart();
                            newCategoriesOpencart
                                    .forEach(nc -> {
                                        if (!categoriesOpencart.contains(nc)) {
                                            categoriesOpencart.add(nc);
                                        }
                                    });

                        } else {
                            productsCategory.add(product);
                        }

                    });
                }
            }
        }
        return productsCategory;
    }

    @Override
    public List<ProductOpencart> getFullProductsData(List<ProductOpencart> products, SupplierApp supplierApp) {
        final String titleCss = "div#full-goods h1[itemprop='name']";
        final String codeCss = "div#full-goods h1[itemprop='name']";
        AtomicInteger count = new AtomicInteger();
        return products
                .stream()
                .peek(p -> {
                    final String urlProduct = p.getUrlProduct();
                    Document webDocument = getWebDocument(urlProduct, new HashMap<>());
                    log.info("{}. Get data from product url: {}", count.addAndGet(1), urlProduct);

                    if (Objects.nonNull(webDocument)) {
                        try {
                            final String name = webDocument.select(titleCss).text().trim();
                            log.info("name: {}", name);

                            final String stringModel = webDocument.select(codeCss).text();
                            log.info("stringModel: {}", stringModel);
                            //fixme: need explanation about model
                            final String model = generateModel(stringModel, "0000");
                            p.setModel(model);
                            final ManufacturerApp manufacturerApp = getManufacturerApp(MANUFACTURER_NAME, supplierApp);
                            final ProductProfileApp firstProfileApp = p.getProductProfileApp();
                            firstProfileApp.setManufacturerId(manufacturerApp.getManufacturerId());
                            firstProfileApp.setManufacturerApp(manufacturerApp);
                            final ProductProfileApp savedProductProfile = getProductProfile(firstProfileApp, supplierApp);
                            log.info("Saved PP Noveen: {}", savedProductProfile);
                            p.setProductProfileApp(savedProductProfile);
                            setManufacturer(p, supplierApp);
                            setPriceWithMarkup(p);
                            String description = getDescription(webDocument);

                            ProductDescriptionOpencart productDescriptionOpencart = new ProductDescriptionOpencart.Builder()
                                    .withDescription(description)
                                    .withName(name)
                                    .withLanguageId(OCConstant.UA_LANGUAGE_ID)
                                    .withMetaH1(name)
                                    .withMetaDescription(name.concat(AppConstant.META_DESCRIPTION_PART))
                                    .withMetaKeyword("Купити ".concat(name))
                                    .build();
                            p.getProductsDescriptionOpencart().add(productDescriptionOpencart);

                            //  fixme: clarify (there are no attributes)
//                            Elements tableAttr = webDocument.select("main.content div[class*=tab-content-attribute] div[class$=inner] tbody>tr");
//                            log.info("tableAttr: {}", tableAttr.size());
//                            List<AttributeWrapper> attributes = tableAttr
//                                    .stream()
//                                    .map(row -> {
//                                        Elements attrElementRow = row.select("td");
//                                        if (attrElementRow.size() >= 2) {
//                                            String keyAttr = attrElementRow.get(0).text().trim();
//                                            String valueAttr = attrElementRow.get(1).text().trim();
//                                            log.info("Key: {}     Value: {}", keyAttr, valueAttr);
//
//                                            AttributeWrapper attributeWrapper = new AttributeWrapper(keyAttr, valueAttr, null);
//                                            log.info("Init attribute: {}", attributeWrapper);
//                                            AttributeWrapper attribute = getAttribute(attributeWrapper, supplierApp, savedProductProfile);
//                                            log.info("Final attribute: {}", attribute);
//                                            if (keyAttr.isEmpty() || valueAttr.isEmpty()) {
//                                                return null;
//                                            }
//                                            return attributeWrapper;
//                                        } else {
//                                            return null;
//                                        }
//
//                                    })
//                                    .filter(Objects::nonNull)
//                                    .collect(Collectors.toList());
//                            p.getAttributesWrapper().addAll(attributes);

                            //  Main image
                            Elements mainImage = webDocument.select("div#full-goods div.img>a");
                            log.info("MainImage: {}", mainImage.attr("href"));
                            if (!mainImage.isEmpty()) {
                                String url = mainImage.attr("href");
                                String format = url.substring(url.lastIndexOf("."));
                                String imageName = p.getModel().concat("_").concat(String.valueOf(1).concat(format));
                                String dbImgPath = AppConstant.PART_DIR_OC_IMAGE.concat(imageName);
                                log.info("image url: {}", url);
                                log.info("image name: {}", imageName);
                                log.info("dbImg path: {}", dbImgPath);
                                downloadImage(url, dbImgPath);
                                p.setImage(dbImgPath);
                            }
                        } catch (Exception ex) {
                            log.warn("Bad parsing product data", ex);
                        }
                    } else {
                        p.setId(-1);
                    }
                })
                .filter(p -> p.getId() != -1)
                .collect(Collectors.toList());
    }

    @Override//Manufacture check
    public ProductProfileApp getProductProfile(ProductProfileApp productProfileApp, SupplierApp supplierApp) {
        List<ProductProfileApp> productProfilesAppDB = supplierApp.getProductProfilesApp();
        boolean contains = productProfilesAppDB.contains(productProfileApp);
        if (contains) {
            productProfileApp = productProfilesAppDB.get(productProfilesAppDB.indexOf(productProfileApp));
        } else {
            int manufacturerId = productProfileApp.getManufacturerId();
            if (manufacturerId == 0) {
                ManufacturerApp manufacturerApp = getManufacturerApp(MANUFACTURER_NAME, supplierApp);
                productProfileApp.setManufacturerId(manufacturerApp.getManufacturerId());
                productProfileApp.setManufacturerApp(manufacturerApp);
            }
            productProfileApp = appDaoService.saveProductProfileApp(productProfileApp);
        }
        return productProfileApp;
    }

    private String getSKUFromHref(String s) {
        String[] strings = s.split("/");
        return strings[strings.length - 1];
    }

    private String generateModel(String supplierModel, String other) {
        String substring = DISPLAY_NAME.substring(0, DISPLAY_NAME.indexOf("-")).trim();
        String result;
        if (supplierModel.isEmpty()) {
            result = substring.concat("--").concat(other);
        } else {
            result = substring.concat("-").concat(supplierModel);
        }
        return result;
    }

    public String getDescription(Document doc) {
        final String descriptionCSS = "div#full-goods div.text[itemprop='description']";
        Elements descElement = doc.select(descriptionCSS);
        String description = !descElement.isEmpty() ? cleanDescription(descElement.get(0)) : "";
        if (description.isEmpty()) {
            log.warn("Description is empty");
        } else {
            log.info("Description UA text: {}", description);
        }
        return wrapToHtml(description);
    }

}

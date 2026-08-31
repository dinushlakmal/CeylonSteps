# Remove the old Icon definition and replace it
sed -i '80,83c\
                        painter = painterResource(id = R.drawable.ic_ceylonsteps_brand_logo),\
                        contentDescription = "CeylonSteps Logo",\
                        tint = Color.Unspecified,\
                        modifier = Modifier.size(38.dp)' app/src/main/java/com/example/ui/components/CeylonStepsBrand.kt

# Replace the subtitle
sed -i 's/text = "SRI LANKA TRAVEL JOURNAL"/text = "TRAVEL TRACKER"/g' app/src/main/java/com/example/ui/components/CeylonStepsBrand.kt


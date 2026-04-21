# =========================================
# IMPORT LIBRARIES
# =========================================
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns

from sklearn.model_selection import train_test_split
from sklearn.preprocessing import LabelEncoder
from sklearn.linear_model import LogisticRegression, LinearRegression
from sklearn.ensemble import RandomForestClassifier
from sklearn.cluster import KMeans
from sklearn.metrics import accuracy_score, mean_squared_error


# =========================================
# LOAD DATASET
# =========================================
df = pd.read_csv("rainfall.csv")

print("Head:\n", df.head())
print("\nInfo:")
df.info()
print("\nDescribe:\n", df.describe(include='all'))
print("\nMissing Values:\n", df.isnull().sum())

# =========================================
# PREPROCESSING
# =========================================
df.drop_duplicates(inplace=True)

# Handle missing values
num_cols = df.select_dtypes(include=np.number)
cat_cols = df.select_dtypes(include=['object', 'string'])

df[num_cols.columns] = num_cols.fillna(num_cols.mean())

for col in cat_cols.columns:
    df[col] = df[col].fillna(df[col].mode()[0])

# Visualization
if not num_cols.empty:
    sns.heatmap(num_cols.corr(), annot=True)
    plt.show()

    num_cols.hist(figsize=(8,6))
    plt.show()
    sns.boxplot(data=num_cols)
    plt.show()
    if len(num_cols.columns) >= 2:
        plt.scatter(df[num_cols.columns[0]], df[num_cols.columns[1]])
        plt.xlabel(num_cols.columns[0])
        plt.ylabel(num_cols.columns[1])
        plt.show()


# =========================================
# CLASSIFICATION
# =========================================
print("\n=== CLASSIFICATION ===")

X = df.iloc[:, :-1]
y = df.iloc[:, -1]

# Encode target if needed
if y.dtype == 'object':
    y = LabelEncoder().fit_transform(y)

X = X.select_dtypes(include=np.number).values  # use only numeric for model

X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2)

models = [
    LogisticRegression(max_iter=1000),
    RandomForestClassifier()
]

for model in models:
    model.fit(X_train, y_train)
    pred = model.predict(X_test)
    print(model.__class__.__name__, "Accuracy:", accuracy_score(y_test, pred))


# =========================================
# REGRESSION
# =========================================
print("\n=== REGRESSION ===")

X_reg = df.select_dtypes(include=np.number).iloc[:, :-1].values
y_reg = df.select_dtypes(include=np.number).iloc[:, -1].values

X_train, X_test, y_train, y_test = train_test_split(X_reg, y_reg, test_size=0.2)

reg = LinearRegression()
reg.fit(X_train, y_train)

y_pred = reg.predict(X_test)

print("MSE:", mean_squared_error(y_test, y_pred))


# =========================================
# CLUSTERING
# =========================================
print("\n=== CLUSTERING ===")

X_cluster = df.select_dtypes(include=np.number).values

kmeans = KMeans(n_clusters=3)
clusters = kmeans.fit_predict(X_cluster)

plt.scatter(X_cluster[:, 0], X_cluster[:, 1], c=clusters)
plt.title("Clustering")
plt.show()
# =========================================
# IR MODEL (TEXT SEARCH)
# =========================================
print("\n=== IR MODEL ===")

text_cols = df.select_dtypes(include='object')

if not text_cols.empty:
    docs = text_cols.astype(str).agg(' '.join, axis=1)

    tfidf = TfidfVectorizer()
    tfidf_matrix = tfidf.fit_transform(docs)

    query = ["rainfall data"]
    query_vec = tfidf.transform(query)

    similarity = cosine_similarity(query_vec, tfidf_matrix)

    print("Similarity Scores:\n", similarity)
else:
    print("No text data available")
